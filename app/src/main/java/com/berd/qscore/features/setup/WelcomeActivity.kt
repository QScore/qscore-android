package com.berd.qscore.features.setup

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.databinding.ActivitySetupBinding
import com.berd.qscore.features.shared.prefs.Prefs
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

        if (Prefs.userLocation != null) {
            toast("Already have location: ${Prefs.userLocation}")
        }
    }

    private fun setupLocation() {
        if (!LocationHelper.hasLocation) {
            lifecycleScope.launch {
                val location = LocationHelper.requestLocation(this@WelcomeActivity, null)
                if (location != null) {
                    toast("Location found: $location")
                    Prefs.userLocation = location
                }
            }
        } else {
            //We have location already (wtf)
            toast("Location found: ${LocationHelper.currentLocation}")
            Prefs.userLocation = LocationHelper.currentLocation
        }
    }
}