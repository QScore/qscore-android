package com.berd.qscore.features.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.type.GeofenceEventType
import com.berd.qscore.utils.location.LocationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    sealed class Event {
        object Entered : Event()
        object Exited : Event()
    }

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        private val eventSubject = ReplaySubject.create<Event>(1)
        val events = eventSubject as Observable<Event>
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = when (geofencingEvent.errorCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> " Geofence service is not available now. Go to Settings>Location>Mode and choose High accuracy."
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Your app has registered too many geofences."
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "You have provided too many PendingIntents to the addGeofences() call."
                else -> "Unknown error receiving geofence event"
            }
            Timber.e(errorMessage)
            return
        }

        // Get the transition type.
        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                eventSubject.onNext(Event.Entered)
                handleEntered()
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                eventSubject.onNext(Event.Exited)
                handleExited()
            }
        }
    }

    private fun handleEntered() = scope.launch {
        Timber.d(">>GEOFENCE ENTERED")
        submitEvent(GeofenceEventType.HOME)
    }

    private fun handleExited() = scope.launch {
        Timber.d(">>GEOFENCE EXITED")
        submitEvent(GeofenceEventType.AWAY)
    }

    private suspend fun submitEvent(eventType: GeofenceEventType) {
        try {
            val location = LocationHelper.fetchLastLocation()
            location?.let {
                Api.createGeofenceEvent(eventType)
            }
        } catch (e: ApolloException) {
            Timber.w("Unable to submit event: $e")
        }
    }
}
