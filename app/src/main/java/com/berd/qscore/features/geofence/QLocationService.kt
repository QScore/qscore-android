package com.berd.qscore.features.geofence

import android.app.Service
import android.content.Intent
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.geofence.GeofenceBroadcastReceiver.Event
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.type.GeofenceEventType
import com.berd.qscore.utils.location.LocationHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber


class QLocationService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1337
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        private const val CHANNEL_NAME = "QLocation Channel"
        private const val LOCATION_UPDATE_INTERVAL_MINUTES = 5L
    }

    private val compositeDisposable = CompositeDisposable()
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        observeGeofenceEvents()
        return START_NOT_STICKY
    }

    private fun observeGeofenceEvents() {
        GeofenceBroadcastReceiver.events.subscribeBy(onNext = {
            handleGeofenceEvent(it)
        }, onError = {
            Timber.e("Unable to handle geofence event: $it")
        }).addTo(compositeDisposable)
    }

    private fun handleGeofenceEvent(event: Event) = when (event) {
        Event.Entered -> handleEntered()
        Event.Exited -> handleExited()
    }

    private fun handleEntered() = scope.launch {
        submitEvent(GeofenceEventType.HOME)
    }

    private fun handleExited() = scope.launch {
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

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        job.cancel()
        compositeDisposable.clear()
        super.onDestroy()
    }

}
