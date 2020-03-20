package com.berd.qscore

import android.app.Application
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.regions.Regions
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.core.Amplify
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.injection.AppInjector
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.logging.LogHelper
import com.facebook.appevents.AppEventsLogger
import timber.log.Timber


class QscoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LogHelper.initializeLogging()
        Injector.initialize(AppInjector(this))
        setupAws()
        setupAmplify()
        setupFacebook()
        setupGeofence()
    }

    private fun setupAws() {
        AWSMobileClient.getInstance()
            .initialize(applicationContext, object : Callback<UserStateDetails> {
                override fun onResult(result: UserStateDetails) {
                    Timber.d("Finished setting up AWSMobileClient")
                }

                override fun onError(e: Exception) {
                    Timber.e("Error setting up AWSMobileClient: $e")
                }
            })
    }

    private fun setupFacebook() {
        AppEventsLogger.activateApp(this)
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