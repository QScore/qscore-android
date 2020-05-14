package com.berd.qscore.features.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.berd.qscore.databinding.ActivitySettingsBinding
import com.berd.qscore.features.login.LoginActivity
import com.berd.qscore.features.settings.SettingsViewModel.SettingsAction
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.username.UsernameActivity
import splitties.activities.start

class SettingsActivity : BaseActivity() {

    private val viewModel by viewModels<SettingsViewModel>()

    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        observeEvents()
        setupToolbar()
        setupViews()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun observeEvents() {
        viewModel.observeActions {
            when (it) {
                SettingsAction.LaunchLoginActivity -> launchLoginActivity()
            }
        }
    }

    private fun launchLoginActivity() {
        start<LoginActivity> {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }

    private fun setupViews() = binding.apply {
        aboutButton.setOnClickListener { TODO() }
        usernameButton.setOnClickListener { launchUsernameActivity() }
        logOutButton.setOnClickListener { viewModel.onLogOut() }
    }

    private fun launchUsernameActivity() {
        val intent = UsernameActivity.newIntent(this, false)
        startActivity(intent)
    }
}
