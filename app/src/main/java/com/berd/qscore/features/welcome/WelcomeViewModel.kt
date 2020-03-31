package com.berd.qscore.features.welcome

import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.ShowError
import com.berd.qscore.features.welcome.WelcomeViewModel.State.FindingLocation
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.coroutines.launch

class WelcomeViewModel : RxViewModel<WelcomeViewModel.Action, WelcomeViewModel.State>() {

    sealed class Action {
        object LaunchScoreActivity : Action()
        object ShowError : Action()
    }

    sealed class State {
        object FindingLocation : State()
        object Ready : State()
    }

    fun setupHome() = viewModelScope.launch {
        state = FindingLocation
        val location = LocationHelper.fetchCurrentLocation()
        if (location != null) {
            Prefs.userLocation = location
            GeofenceHelper.setGeofence(location)
            state = State.Ready
            action(LaunchScoreActivity)
        } else {
            state = State.Ready
            action(ShowError)
        }
    }
}