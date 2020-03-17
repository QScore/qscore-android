package com.berd.qscore.features.setup

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.databinding.ActivitySetupBinding
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.coroutines.launch
import splitties.toast.toast

class WelcomeActivity : AppCompatActivity() {

    private val binding: ActivitySetupBinding by lazy { ActivitySetupBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<WelcomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        setupViews()
    }

    private fun setupViews() = binding.let {
        it.userHomeButton.setOnClickListener {
            setupLocation()
        }
    }

    private fun setupLocation() {
        if (!LocationHelper.hasLocation) {
            lifecycleScope.launch {
                val location = LocationHelper.requestLocation(this@WelcomeActivity, null)
                if (location != null) {
                    toast("Location found: $location")
                }
            }
        } else {
            //We have location already
            toast("Location found: ${LocationHelper.currentLocation}")
        }
    }
}