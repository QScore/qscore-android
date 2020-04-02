package com.berd.qscore.features.score

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityScoreBinding
import com.berd.qscore.features.geofence.GeofenceState.*
import com.berd.qscore.features.geofence.QLocationService
import com.berd.qscore.features.login.LoginActivity
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.visible
import com.berd.qscore.utils.location.LocationHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.launch
import splitties.activities.start
import splitties.toast.toast
import timber.log.Timber


class ScoreActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val viewModel by viewModels<ScoreViewModel>()

    private val binding: ActivityScoreBinding by lazy {
        ActivityScoreBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeEvents()
        setupViews()
        startLocationService()
        viewModel.onCreate()
    }

    private fun startLocationService() {
        if (LocationHelper.hasAllPermissions) {
            val serviceIntent = Intent(this, QLocationService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            toast("Location permissions are not available!")
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun checkLocation() = lifecycleScope.launch {
        if (LocationHelper.hasAllPermissions) {
            LocationHelper.fetchCurrentLocation()
        }
    }

    private fun observeEvents() {
        viewModel.actions.subscribeBy(onNext = {
            handleActions(it)
        }, onError = {
            Timber.e("Error subscribing to events: $it")
        }).addTo(compositeDisposable)

        viewModel.viewState.observe(this, Observer { state ->
            when (state) {
                Unknown -> handleStartingUp()
                Home -> handleHome()
                Away -> handleAway()
            }
        })
    }

    private fun handleStartingUp() = with(binding) {
        startupText.visible()
        awayText.gone()
        homeText.gone()
    }

    private fun handleHome() = with(binding) {
        startupText.gone()
        awayText.gone()
        homeText.visible()
    }

    private fun handleAway() = with(binding) {
        startupText.gone()
        awayText.visible()
        homeText.gone()
    }

    private fun handleActions(it: ScoreViewModel.Action) {
        when (it) {
            ScoreViewModel.Action.LaunchLoginActivity -> launchLoginActivity()
        }
    }

    private fun launchLoginActivity() {
        start<LoginActivity>()
        finish()
    }

    private fun setupViews() = binding.apply {
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            true
        }

        R.id.action_donate -> {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7YYE6BBQZU4BQ&source=url"))
            startActivity(browserIntent)
            true
        }

        R.id.action_logout -> {
            viewModel.onLogout()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}