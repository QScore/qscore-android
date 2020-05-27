package com.berd.qscore.features.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.utils.injection.Injector
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

enum class GeofenceStatus {
    HOME,
    AWAY
}

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private val userRepository by lazy { Injector.userRepository }

    companion object {
        private val eventSubject = ReplaySubject.create<GeofenceStatus>(1)
        val events = eventSubject as Observable<GeofenceStatus>
        var currentStatus = GeofenceStatus.AWAY
            private set
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

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                eventSubject.onNext(GeofenceStatus.HOME)
                currentStatus = GeofenceStatus.HOME
                handleEntered()
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                eventSubject.onNext(GeofenceStatus.AWAY)
                currentStatus = GeofenceStatus.AWAY
                handleExited()
            }
        }
    }

    private fun handleEntered() = scope.launch {
        submitEvent(GeofenceStatus.HOME)
    }

    private fun handleExited() = scope.launch {
        submitEvent(GeofenceStatus.AWAY)
    }

    private suspend fun submitEvent(status: GeofenceStatus) {
        try {
            userRepository.createGeofenceEvent(status)
        } catch (e: ApolloException) {
            Timber.w("Unable to submit event: $e")
        }
    }
}
