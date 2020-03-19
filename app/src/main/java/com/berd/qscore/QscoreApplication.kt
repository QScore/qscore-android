package com.berd.qscore

import android.app.Application
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.core.Amplify
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
            Timber.d("Finished setting up amplify")
        } catch (e: AmplifyException) {
            Timber.e("Unable to setup amplify: $e")
        }
    }

    private fun setupGeofence() {
        Prefs.userLocation?.let { location ->
            GeofenceHelper.addGeofence(location)
        }
    }
}