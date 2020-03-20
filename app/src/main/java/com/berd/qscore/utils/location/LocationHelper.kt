package com.berd.qscore.utils.location

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.berd.qscore.features.shared.ui.LocationServicesDialogFragment
import com.berd.qscore.utils.activityresult.intentSenderForResult
import com.berd.qscore.utils.extensions.toLatLngPair
import com.berd.qscore.utils.injection.Injector
import com.github.florent37.runtimepermission.kotlin.PermissionException
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

    private var hasPromptedForSettings = false
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    val hasLocation get() = currentLocation != null

    val singleLocationRequest = LocationRequest().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 0
        numUpdates = 1
    }

    private val permissions = arrayListOf<String>(
        ACCESS_COARSE_LOCATION,
        ACCESS_BACKGROUND_LOCATION
    )

    val hasLocationPermissions: Boolean
        get() {
            return permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }

    var currentLocation: LatLngPair? = null
        private set

    suspend fun setupLocationPermission(activity: FragmentActivity, fragment: Fragment?): Boolean {
        return try {
            if (!hasLocationPermissions) {
                val hasPermission = requestLocationPermission(activity, fragment)
                if (!hasPermission) {
                    return false
                }
            }
            checkLocationSettings(activity)
        } catch (e: ApiException) {
            //Location settings are not adequate
            handleLocationSettingsError(activity, fragment, e)
            false
        } catch (e: IntentSender.SendIntentException) {
            false
        } catch (e: PermissionException) {
            false
        }
    }

    suspend fun requestCurrentLocationWithPermission(
        activity: FragmentActivity,
        fragment: Fragment?
    ): LatLngPair? {
        if (!setupLocationPermission(activity, fragment)) {
            return null
        }
        currentLocation = fetchCurrentLocation()?.toLatLngPair()
        return currentLocation
    }

    suspend fun requestLastLocationWithPermission(
        activity: FragmentActivity,
        fragment: Fragment?
    ): LatLngPair? {
        if (!setupLocationPermission(activity, fragment)) {
            return null
        }
        currentLocation = fetchLastOrCurrentLocation()
        return currentLocation
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

    private suspend fun fetchLastOrCurrentLocation(): LatLngPair? {
        return fetchLastLocation()?.toLatLngPair() ?: fetchCurrentLocation()?.toLatLngPair()
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLastLocation() = suspendCoroutine<Location?> {
        locationClient.lastLocation.addOnSuccessListener { location ->
            it.resume(location)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentLocation() = suspendCoroutine<Location?> {
        locationClient.requestLocationUpdates(singleLocationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                it.resume(locationResult?.lastLocation)
            }
        }, null)
    }

    private suspend fun requestLocationPermission(
        activity: FragmentActivity,
        fragment: Fragment?
    ): Boolean {
        return permissions.all {
            val result = fragment?.askPermission(it) ?: activity.askPermission(it)
            result.isAccepted
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
        if (hasPromptedForSettings) {
            Timber.d("locationSettingsCallback: failure, already prompted")
            return
        }

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
        hasPromptedForSettings = true
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
