package com.berd.qscore.features.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityMainBinding
import com.berd.qscore.features.geofence.QLocationService
import com.berd.qscore.features.login.LoginActivity
import com.berd.qscore.features.main.MainViewModel.MainAction.LaunchLoginActivity
import com.berd.qscore.features.main.bottomnav.BottomTab
import com.berd.qscore.features.main.bottomnav.BottomTab.*
import com.berd.qscore.features.main.bottomnav.FragmentStateManager
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.utils.location.LocationHelper
import splitties.activities.start
import splitties.toast.toast


class MainActivity : BaseActivity() {

    private val viewModel by viewModels<MainViewModel>()

    private val fragmentStateManager: FragmentStateManager by lazy {
        FragmentStateManager(binding.fragmentContainer, supportFragmentManager)
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        startLocationService()
        observeEvents()
        setupViews()
        setupBottomNav()
    }

    private fun setupBottomNav() = with(binding.bottomNavigation) {
        changeFragment(ME)
        setOnNavigationItemSelectedListener {
            val bottomTab = BottomTab.fromMenuItemId(it.itemId)
            changeFragment(bottomTab)
            true
        }
        setOnNavigationItemReselectedListener {
            // Do nothing
        }
    }

    private fun changeFragment(bottomTab: BottomTab) {
        fragmentStateManager.changeFragment(bottomTab)
        val toolbarTitleResId = when (bottomTab) {
            ME -> R.string.me
            SEARCH -> R.string.search
            LEADERBOARD -> R.string.leaderboards
        }
        supportActionBar?.title = getString(toolbarTitleResId)
    }

    private fun observeEvents() {
        viewModel.observeActions {
            when (it) {
                LaunchLoginActivity -> launchLoginActivity()
            }
        }
    }

    private fun setupViews() = binding.apply {
        setSupportActionBar(toolbar)
        //Set up bottom tabs
    }

    private fun startLocationService() {
        if (LocationHelper.hasAllPermissions) {
            val serviceIntent = Intent(this, QLocationService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            toast("Location permissions are not available!")
        }
    }

    private fun launchLoginActivity() {
        start<LoginActivity>()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
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
