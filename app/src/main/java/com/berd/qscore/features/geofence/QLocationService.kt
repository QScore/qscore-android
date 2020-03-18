package com.berd.qscore.features.geofence

import android.app.*
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.berd.qscore.R
import com.berd.qscore.features.score.ScoreActivity


class QLocationService : Service() {
    val CHANNEL_ID = "ForegroundServiceChannel"

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notificationIntent = Intent(this, ScoreActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QScore")
            .setContentText("Tracking location")
            .setSmallIcon(R.drawable.ic_baseline_score_24)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent?) = null

}