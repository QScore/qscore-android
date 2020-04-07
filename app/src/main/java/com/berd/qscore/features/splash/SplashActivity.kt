package com.berd.qscore.features.splash

import android.os.Bundle
import androidx.activity.viewModels
import com.berd.qscore.databinding.ActivitySplashBinding
import com.berd.qscore.features.login.LoginActivity
import com.berd.qscore.features.login.SelectUsernameActivity
import com.berd.qscore.features.login.SignUpActivity
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.splash.Action.*
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
        viewModel.onCreate()
    }

    private fun observeViewModel() {
        viewModel.observeActions { action ->
            when (action) {
                LaunchWelcomeActivity -> launchWelcomeActivity()
                LaunchScoreActivity -> launchScoreActivity()
                LaunchLoginActivity -> launchLoginActivity()
                LaunchSignUpActivity -> launchSignUpActivity()
                LaunchUsernameActivity -> launchUsernameActivity()
            }
        }
    }

    private fun launchUsernameActivity() {
        start<SelectUsernameActivity>()
        finish()
    }

    private fun launchSignUpActivity() {
        start<SignUpActivity>()
        finish()
    }

    private fun launchLoginActivity() {
        start<LoginActivity>()
        finish()
    }

    private fun launchScoreActivity() {
        start<ScoreActivity>()
        finish()
    }

    private fun launchWelcomeActivity() {
        start<WelcomeActivity>()
        finish()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}