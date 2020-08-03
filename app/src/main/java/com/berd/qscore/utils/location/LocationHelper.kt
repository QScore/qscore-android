package com.berd.qscore.utils.location

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.berd.qscore.R
import com.berd.qscore.utils.activityresult.intentSenderForResult
import com.berd.qscore.utils.dialog.showDialogFragment
import com.berd.qscore.utils.extensions.hasPermissions
import com.berd.qscore.utils.extensions.toLatLngPair
import com.berd.qscore.utils.injection.Injector
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class LocationHelper(private val locationClient: FusedLocationProviderClient) {
    private val context by lazy { Injector.appContext }

    private val singleLocationRequest = LocationRequest().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 0
        numUpdates = 1
    }

    private val allPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION)
    } else {
        arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    }
    val hasAllPermissions get() = context.hasPermissions(*allPermissions)

    suspend fun checkPermissions(activity: FragmentActivity): Boolean {
        return if (activity.hasPermissions(*allPermissions)) {
            true
        } else {
            activity.askPermission(*allPermissions).isAccepted
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(intervalMillis: Long, callback: (LocationResult) -> Unit) {
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            interval = intervalMillis
        }

        locationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                callback(locationResult)
            }
        }, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchLastLocation() = suspendCoroutine<LatLngPair?> {
        locationClient.lastLocation.addOnSuccessListener { location ->
            it.resume(location?.toLatLngPair())
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(looper: Looper? = null) = suspendCancellableCoroutine<LatLngPair?> {
        if (!hasAllPermissions) {
            it.resumeWithException(IllegalStateException("Cannot fetch current location, permissions not granted"))
            return@suspendCancellableCoroutine
        }
        try {
            locationClient.requestLocationUpdates(singleLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    Timber.d("Location result: $locationResult")
                    it.resume(locationResult?.lastLocation?.toLatLngPair())
                }
            }, looper)
        } catch (e: Exception) {
            Timber.d("Unable to get location")
            it.resumeWithException(e)
        }
    }

    //Throws exception if settings are not adequate
    private suspend fun checkLocationSettings(activity: FragmentActivity) =
        suspendCoroutine<Boolean> { cont ->
            val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(singleLocationRequest)
                .build()
            LocationServices.getSettingsClient(activity)
                .checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun handleLocationSettingsError(
        activity: FragmentActivity,
        fragment: Fragment?,
        e: ApiException
    ) {
        when (e.statusCode) {
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> launchSettings(
                activity,
                fragment,
                e as ResolvableApiException
            )
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> handleManualSettingsRequired(
                activity
            )
        }
    }

    private suspend fun launchSettings(
        activity: FragmentActivity,
        fragment: Fragment?,
        e: ResolvableApiException
    ) = suspendCoroutine<Unit> {
        try {
            if (fragment != null) {
                fragment.intentSenderForResult(e.resolution.intentSender) { result ->
                    if (result.isResultOk) {
                        it.resume(Unit)
                    }
                }
            } else {
                activity.intentSenderForResult(e.resolution.intentSender) { result ->
                    if (result.isResultOk) {
                        it.resume(Unit)
                    }
                }
            }
        } catch (sie: IntentSender.SendIntentException) {
            Timber.d("PendingIntent unable to execute request.")
            it.resumeWithException(sie)
        }
    }

    private fun handleManualSettingsRequired(activity: FragmentActivity) {
        Timber.e("Location settings are inadequate, and cannot be fixed here. Fix in Settings.")
        showLocationServicesDialog(activity)
    }

    private fun showLocationServicesDialog(activity: FragmentActivity) = activity.showDialogFragment {
        title(R.string.location_error_title)
        message(R.string.location_error_message)
        positiveButtonResId(R.string.location_error_settings)
        positiveButton {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        negativeButtonResId(R.string.location_error_not_now)
    }
}
