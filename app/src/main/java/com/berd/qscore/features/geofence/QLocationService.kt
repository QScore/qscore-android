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
import com.berd.qscore.features.score.ScoreActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import splitties.intents.toPendingActivity
import timber.log.Timber


class QLocationService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1337
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        private const val CHANNEL_NAME = "QLocation Channel"
    }

    private val compositeDisposable = CompositeDisposable()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startService("Starting up...")
        observeGeofenceEvents()
        return START_NOT_STICKY
    }

    private fun startService(message: String) =
        startForeground(NOTIFICATION_ID, buildNotification(message))


    private fun observeGeofenceEvents() {
        GeofenceIntentService.events.subscribeBy(onNext = {
            handleGeofenceEvent(it)
        }, onError = {
            Timber.e("Unable to handle geofence event: $it")
        }).addTo(compositeDisposable)
    }

    private fun handleGeofenceEvent(event: Event) = when (event) {
        Entered -> updateNotification("You are home!")
        Exited -> updateNotification("You are not at home!")
    }

    private fun buildNotification(message: String): Notification {
        val pendingIntent = Intent(this, ScoreActivity::class.java).toPendingActivity(
            reqCode = 0,
            flags = PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QScore")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_baseline_score_24)
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private fun updateNotification(message: String) {
        val notification = buildNotification(message)
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
        compositeDisposable.clear()
        super.onDestroy()
    }

}