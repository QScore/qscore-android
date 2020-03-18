package com.berd.qscore.utils.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import com.berd.qscore.features.geofence.GeofenceIntentService
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.location.LatLngPair
import com.berd.qscore.utils.location.LocationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import timber.log.Timber

object GeofenceHelper {

    private val context = Injector.appContext
    const val REQUEST_ID = "GEOFENCE_REQUEST_ID"

    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getService(
            context,
            0,
            Intent(context, GeofenceIntentService::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private val geofenceClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(
            context
        )
    }

    fun addGeofence(location: LatLngPair) {
        val geofence = Geofence.Builder()
            .setRequestId(REQUEST_ID)
            .setCircularRegion(
                location.lat,
                location.lng,
                50f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val request = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            addGeofences(arrayListOf(geofence))
        }.build()

        @SuppressLint("MissingPermission")
        if (LocationHelper.hasLocationPermissions) {
            geofenceClient.addGeofences(request, pendingIntent)
                .addOnSuccessListener {
                    Timber.d("Successfully created geofence: $geofence")
                }.addOnFailureListener {
                    Timber.e("Error creating geofence: $it")
                }
        }
    }

    fun clearGeofences() {
        geofenceClient.removeGeofences(pendingIntent)
            .addOnSuccessListener {
                Timber.d("Successfully cleared geofences")
            }.addOnFailureListener {
                Timber.e("Error clearing geofences: $it")
            }
    }


}