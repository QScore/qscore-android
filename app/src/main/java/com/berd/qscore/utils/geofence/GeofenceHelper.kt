package com.berd.qscore.utils.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import com.berd.qscore.features.geofence.GeofenceBroadcastReceiver
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.location.LatLngPair
import com.berd.qscore.utils.location.LocationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import timber.log.Timber

class GeofenceHelper {
    private val userRepository by lazy { Injector.userRepository }
    private val geofencingClient by lazy { Injector.geofencingClient }
    private val locationHelper by lazy { Injector.locationHelper }
    private val appContext by lazy { Injector.appContext }

    companion object {
        const val REQUEST_ID = "GEOFENCE_REQUEST_ID"
        const val GEOFENCE_RADIUS = 150f //meters
    }

    private val pendingIntentBroadcast: PendingIntent by lazy {
        val intent = Intent(appContext, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun setGeofence(location: LatLngPair) {
        clearGeofences()
        val geofence = Geofence.Builder()
            .setRequestId(REQUEST_ID)
            .setCircularRegion(
                location.lat,
                location.lng,
                GEOFENCE_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val request = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            addGeofences(arrayListOf(geofence))
        }.build()

        @SuppressLint("MissingPermission")
        if (locationHelper.hasAllPermissions) {
            geofencingClient.addGeofences(request, pendingIntentBroadcast)
                .addOnSuccessListener {
                    Timber.d("Successfully created geofence: $geofence")
                }.addOnFailureListener {
                    Timber.e("Error creating geofence: $it")
                }
        }
    }

    fun clearGeofences() {
        geofencingClient.removeGeofences(pendingIntentBroadcast)
            .addOnSuccessListener {
                Timber.d("Successfully cleared geofences")
            }.addOnFailureListener {
                Timber.e("Error clearing geofences: $it")
            }
    }


}
