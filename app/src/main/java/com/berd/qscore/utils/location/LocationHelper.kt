package com.berd.qscore.utils.location

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.IntentSender
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.berd.qscore.features.shared.ui.LocationServicesDialogFragment
import com.berd.qscore.utils.activityresult.intentSenderForResult
import com.berd.qscore.utils.extensions.hasPermissions
import com.berd.qscore.utils.extensions.toLatLngPair
import com.berd.qscore.utils.injection.Injector
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


object LocationHelper {
    private val context = Injector.appContext
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    val singleLocationRequest = LocationRequest().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 0
        numUpdates = 1
    }

    val hasFineLocationPermission get() = context.hasPermissions(ACCESS_FINE_LOCATION)
    val hasBackgroundLocationPermission get() = context.hasPermissions(ACCESS_BACKGROUND_LOCATION)
    val hasAllPermissions get() = context.hasPermissions(ACCESS_COARSE_LOCATION)

    suspend fun checkPermissions(activity: FragmentActivity): Boolean {
        return if (activity.hasPermissions(ACCESS_COARSE_LOCATION)) {
            if (activity.hasPermissions(ACCESS_FINE_LOCATION)) {
                true
            } else {
                activity.askPermission(ACCESS_FINE_LOCATION).isAccepted
            }
        } else {
            activity.askPermission(ACCESS_COARSE_LOCATION).isAccepted
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
    suspend fun fetchCurrentLocation() = suspendCoroutine<LatLngPair?> {
        locationClient.requestLocationUpdates(singleLocationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                it.resume(locationResult?.lastLocation?.toLatLngPair())
            }
        }, null)
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
        LocationServicesDialogFragment().show(activity.supportFragmentManager, "dialog")
    }
}
