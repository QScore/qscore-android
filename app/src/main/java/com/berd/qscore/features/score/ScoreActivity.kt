package com.berd.qscore.features.score

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityScoreBinding
import com.berd.qscore.features.geofence.QLocationService
import com.berd.qscore.features.login.LoginActivity
import com.berd.qscore.features.score.ScoreViewModel.ScoreState
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.visible
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.coroutines.launch
import splitties.activities.start
import splitties.toast.toast


class ScoreActivity : BaseActivity() {

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
        viewModel.onResume()
        lifecycleScope.launch {
            LocationHelper.fetchCurrentLocation()
        }
    }

    private fun observeEvents() {
        viewModel.observeActions {
            handleActions(it)
        }

        viewModel.observeState {
            handleState(it)
        }
    }

    private fun handleState(state: ScoreState) = with(binding) {
        when (state) {
            is ScoreState.Loading -> handleLoading()
            is ScoreState.Ready -> handleReady(state.score)
        }
    }

    private fun handleLoading() = with(binding) {
        progress.visible()
        qscoreValue.invisible()
    }

    private fun handleReady(score: String) = with(binding) {
        progress.invisible()
        qscoreValue.visible()
        qscoreValue.text = score
    }

    private fun handleActions(it: ScoreViewModel.ScoreAction) {
        when (it) {
            ScoreViewModel.ScoreAction.LaunchLoginActivity -> launchLoginActivity()
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
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7YYE6BBQZU4BQ&source=url")
            )
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