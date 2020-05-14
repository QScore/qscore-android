package com.berd.qscore.features.splash

import android.os.Bundle
import androidx.activity.viewModels
import com.berd.qscore.databinding.ActivitySplashBinding
import com.berd.qscore.features.geofence.UpdateLocationWorker
import com.berd.qscore.features.login.LoginActivity
import com.berd.qscore.features.main.MainActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.splash.Action.*
import com.berd.qscore.features.username.UsernameActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import splitties.activities.start

class SplashActivity : BaseActivity() {

    private val viewModel by viewModels<SplashViewModel>()

    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeViewModel()
        setupLocationUpdates()
        viewModel.onCreate()
    }

    private fun setupLocationUpdates() {
        UpdateLocationWorker.schedule(this)
    }

    private fun observeViewModel() {
        viewModel.observeActions { action ->
            when (action) {
                LaunchWelcomeActivity -> launchWelcomeActivity()
                LaunchScoreActivity -> launchScoreActivity()
                LaunchLoginActivity -> launchLoginActivity()
                LaunchUsernameActivity -> launchUsernameActivity()
            }
        }
    }

    private fun launchUsernameActivity() {
        val intent = UsernameActivity.newIntent(this, true)
        startActivity(intent)
        finish()
    }

    private fun launchLoginActivity() {
        start<LoginActivity>()
        finish()
    }

    private fun launchScoreActivity() {
        start<MainActivity>()
        finish()
    }

    private fun launchWelcomeActivity() {
        start<WelcomeActivity>()
        finish()
    }

    override fun getScreenName() = "splash"

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
