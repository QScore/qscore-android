package com.berd.qscore.features.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityMainBinding
import com.berd.qscore.features.login.LoginActivity
import com.berd.qscore.features.main.MainViewModel.MainAction.LaunchLoginActivity
import com.berd.qscore.features.main.bottomnav.BottomTab
import com.berd.qscore.features.main.bottomnav.FragmentStateManager
import com.berd.qscore.features.shared.activity.BaseActivityWithState
import splitties.activities.start


class MainActivity : BaseActivityWithState() {

    private val viewModel by viewModels<MainViewModel>()

    private val fragmentStateManager: FragmentStateManager by lazy {
        FragmentStateManager(R.id.fragmentContainer, supportFragmentManager)
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeEvents()
        viewModel.onCreate()
        setupBottomNav()
    }

    private fun setupBottomNav() = with(binding.bottomNavigation) {
        setOnNavigationItemSelectedListener {
            viewModel.onBottomTabSelected(it.itemId)
            true
        }
        setOnNavigationItemReselectedListener {
            // Do nothing
        }
    }

    private fun observeEvents() {
        viewModel.observeActions {
            when (it) {
                LaunchLoginActivity -> launchLoginActivity()
                is MainViewModel.MainAction.Initialize -> initialize(it.state)
                is MainViewModel.MainAction.ChangeTab -> changeFragment(it.tab)
            }
        }
    }

    private fun initialize(state: MainViewModel.MainState) {
        changeFragment(state.selectedTab)
    }

    private fun changeFragment(bottomTab: BottomTab) {
        fragmentStateManager.changeTab(bottomTab)
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
