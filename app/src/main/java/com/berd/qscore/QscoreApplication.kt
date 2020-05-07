package com.berd.qscore

import android.app.Application
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.injection.InjectorImpl
import com.berd.qscore.utils.logging.LogHelper
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth


class QscoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LogHelper.initializeLogging()
        setupInjection()
        setupFacebook()
        setupGeofence()
    }

    private fun setupInjection() {
        val appInjector = InjectorImpl(
            appContext = this,
            firebaseAuth = FirebaseAuth.getInstance(),
            fbLoginmanager = LoginManager.getInstance()
        )
        Injector.initialize(appInjector)
    }

    private fun setupFacebook() {
        AppEventsLogger.activateApp(this)
    }

    private fun setupGeofence() {
        Prefs.userLocation?.let { location ->
            GeofenceHelper.setGeofence(location)
        }
    }
}
