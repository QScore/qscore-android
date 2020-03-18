package com.berd.qscore.features.score

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.databinding.ActivityScoreBinding
import com.berd.qscore.features.score.ScoreViewModel.State.Away
import com.berd.qscore.features.score.ScoreViewModel.State.Home
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.visible
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.coroutines.launch

class ScoreActivity : AppCompatActivity() {

    private val viewModel by viewModels<ScoreViewModel>()

    private val binding: ActivityScoreBinding by lazy {
        ActivityScoreBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeEvents()
        viewModel.onCreate()
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun checkLocation() = lifecycleScope.launch {
        LocationHelper.requestCurrentLocation(this@ScoreActivity, null)
    }

    private fun observeEvents() {
        viewModel.viewState.observe(this, Observer { state ->
            when (state) {
                Home -> handleHome()
                Away -> handleAway()
            }
        })
    }

    private fun handleHome() = binding.apply {
        awayText.gone()
        homeText.visible()
    }

    private fun handleAway() = binding.apply {
        awayText.visible()
        homeText.gone()
    }
}