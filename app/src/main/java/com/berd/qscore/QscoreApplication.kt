package com.berd.qscore

import android.app.Application
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.injection.InjectorImpl
import com.berd.qscore.utils.logging.LogHelper
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
            fbLoginmanager = LoginManager.getInstance(),
            googleSignInClient = buildGoogleSignInClient()
        )
        Injector.initialize(appInjector)
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
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
