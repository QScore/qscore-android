package com.berd.qscore

import android.app.Application
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.injection.AppInjector
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.logging.LogHelper


class QscoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LogHelper.initializeLogging()
        Injector.initialize(AppInjector(this))
        setupGeofence()
    }

    private fun setupGeofence() {
        Prefs.userLocation?.let { location ->
            GeofenceHelper.addGeofence(location)
        }
    }
}