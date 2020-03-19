package com.berd.qscore.features.geofence

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.berd.qscore.R
import com.berd.qscore.features.geofence.GeofenceIntentService.Event
import com.berd.qscore.features.geofence.GeofenceIntentService.Event.Entered
import com.berd.qscore.features.geofence.GeofenceIntentService.Event.Exited
import com.berd.qscore.features.geofence.GeofenceState.*
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.utils.location.LocationHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import splitties.intents.toPendingActivity
import timber.log.Timber
import java.util.concurrent.TimeUnit


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startService(Unknown)
        observeGeofenceEvents()
        setupLocationUpdates()
        return START_NOT_STICKY
    }

    private fun setupLocationUpdates() = scope.launch {
        LocationHelper.fetchCurrentLocation()
        val interval = TimeUnit.MINUTES.toMillis(LOCATION_UPDATE_INTERVAL_MINUTES)
        LocationHelper.startLocationUpdates(interval) {
            Timber.d("Updated location: ${it.lastLocation}")
        }
    }

    private fun startService(state: GeofenceState) =
        startForeground(NOTIFICATION_ID, buildNotification(state))


    private fun observeGeofenceEvents() {
        GeofenceIntentService.events.subscribeBy(onNext = {
            handleGeofenceEvent(it)
        }, onError = {
            Timber.e("Unable to handle geofence event: $it")
        }).addTo(compositeDisposable)
    }

    private fun handleGeofenceEvent(event: Event) = when (event) {
        Entered -> { updateNotification(Home) }
        Exited -> { updateNotification(Away) }
    }

    private fun buildNotification(state: GeofenceState): Notification {
        val pendingIntent = Intent(this, ScoreActivity::class.java).toPendingActivity(
            reqCode = 0,
            flags = PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QScore")
            .setContentText(state.message)
            .setSmallIcon(R.drawable.ic_baseline_score_24)
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private fun updateNotification(state: GeofenceState) {
        val notification = buildNotification(state)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        job.cancel()
        compositeDisposable.clear()
        super.onDestroy()
    }

}