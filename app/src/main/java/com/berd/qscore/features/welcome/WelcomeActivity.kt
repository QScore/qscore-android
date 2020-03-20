package com.berd.qscore.features.welcome

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.databinding.ActivitySetupBinding
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.coroutines.launch
import splitties.activities.start
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
            lifecycleScope.launch {
                saveCurrentLocation()
            }
        }
    }

    private suspend fun saveCurrentLocation() {
        if (LocationHelper.checkPermissions(this)) {
            val location = LocationHelper.fetchCurrentLocation()
            if (location != null) {
                toast("Location found: $location")
                Prefs.userLocation = location
                GeofenceHelper.clearGeofences()
                GeofenceHelper.addGeofence(location)
                start<ScoreActivity>()
            } else {
                if (location == null) {
                    toast("Unable to retrieve location.  Please try again later")
                }
            }
        } else {
            toast("You must allow permissions to continue")
        }
    }
}