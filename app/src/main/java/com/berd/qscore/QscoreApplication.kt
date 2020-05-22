package com.berd.qscore

import android.app.Application
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.injection.InjectorImpl
import com.berd.qscore.utils.location.LocationHelper
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.stringcare.library.SC
import timber.log.Timber


class QscoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
        setupInjection()
        setupStringCare()
        setupGeofence()
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun setupStringCare() {
        SC.Companion.init(this)
    }

    private fun setupInjection() {
        val appInjector = InjectorImpl(
            appContext = this,
            firebaseAuth = FirebaseAuth.getInstance(),
            fbLoginmanager = LoginManager.getInstance(),
            googleSignInClient = buildGoogleSignInClient(),
            geofencingClient = LocationServices.getGeofencingClient(this),
            geofenceHelper = GeofenceHelper(),
            userRepository = UserRepository(),
            api = Api(),
            locationHelper = buildLocationHelper()
        )
        Injector.initialize(appInjector)
    }

    private fun buildLocationHelper(): LocationHelper {
        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        return LocationHelper(locationClient)
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    private fun setupGeofence() {
        Prefs.userLocation?.let { location ->
            Injector.geofenceHelper.setGeofence(location)
        }
    }
}
