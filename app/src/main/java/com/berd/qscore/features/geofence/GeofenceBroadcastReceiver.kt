package com.berd.qscore.features.geofence

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes.*
import com.google.android.gms.location.GeofencingEvent
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import timber.log.Timber

class GeofenceIntentService : IntentService(GeofenceIntentService.javaClass.simpleName) {

    sealed class Event {
        object Entered : Event()
        object Exited : Event()
    }

    companion object {
        private val eventSubject = ReplaySubject.create<Event>(1)
        val events = eventSubject as Observable<Event>
    }

    override fun onHandleIntent(intent: Intent?) {
        Timber.d(">>Handling intent: $intent")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = when (geofencingEvent.errorCode) {
                GEOFENCE_NOT_AVAILABLE -> " Geofence service is not available now. Go to Settings>Location>Mode and choose High accuracy."
                GEOFENCE_TOO_MANY_GEOFENCES -> "Your app has registered too many geofences."
                GEOFENCE_TOO_MANY_PENDING_INTENTS -> "You have provided too many PendingIntents to the addGeofences() call."
                else -> "Unknown error receiving geofence event"
            }
            Timber.e(errorMessage)
            return
        }

        // Get the transition type.
        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Timber.d(">>User came home")
                eventSubject.onNext(Event.Entered)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Timber.d(">>User left home")
                eventSubject.onNext(Event.Exited)
            }
        }
    }
}