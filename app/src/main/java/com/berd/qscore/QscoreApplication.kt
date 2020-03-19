package com.berd.qscore

import android.app.Application
import android.util.Log
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.injection.AppInjector
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.logging.LogHelper
import timber.log.Timber


class QscoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LogHelper.initializeLogging()
        Injector.initialize(AppInjector(this))
        setupAmplify()
        setupGeofence()
    }

    private fun setupAmplify() {
        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(applicationContext)
            Log.i("AmplifyGetStarted", "Amplify is all setup and ready to go!")
        } catch (exception: AmplifyException) {
            Timber.d(exception.getMessage())
        }
    }

    private fun setupGeofence() {
        Prefs.userLocation?.let { location ->
            GeofenceHelper.addGeofence(location)
        }
    }
}