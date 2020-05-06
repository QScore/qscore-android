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
import com.berd.qscore.features.main.bottomnav.BottomTab.ME
import com.berd.qscore.features.main.bottomnav.FragmentStateManager
import com.berd.qscore.features.shared.activity.BaseActivity
import splitties.activities.start


class MainActivity : BaseActivity() {

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
        fragmentStateManager.changeTab(bottomTab)
    }

    private fun observeEvents() {
        viewModel.observeActions {
            when (it) {
                LaunchLoginActivity -> launchLoginActivity()
            }
        }
    }

    private fun setupViews() = binding.apply {
        //Set up bottom tabs
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
