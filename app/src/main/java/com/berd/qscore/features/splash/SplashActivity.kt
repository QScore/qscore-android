package com.berd.qscore.features.splash

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.databinding.ActivitySplashBinding
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.splash.Action.LaunchScoreActivity
import com.berd.qscore.features.splash.Action.LaunchWelcomeActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import splitties.activities.start
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

    private val viewModel by viewModels<SplashViewModel>()
    private val compositeDisposable = CompositeDisposable()

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
        viewModel.events.subscribeBy(onNext = { action ->
            when (action) {
                LaunchWelcomeActivity -> launchWelcomeActivity()
                LaunchScoreActivity -> launchScoreActivity()
            }
        }, onError = {
            Timber.e("Unable to subscribe to events: $it")
        }).addTo(compositeDisposable)
    }

    private fun launchScoreActivity() {
        start<ScoreActivity>()
    }

    private fun launchWelcomeActivity() {
        start<WelcomeActivity>()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}