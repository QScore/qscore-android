package com.berd.qscore.features.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.ShowError
import com.berd.qscore.features.welcome.WelcomeViewModel.State.FindingLocation
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.location.LocationHelper
import com.berd.qscore.utils.rx.RxEventSender
import kotlinx.coroutines.launch

class WelcomeViewModel : ViewModel() {

    private val _actions = RxEventSender<Action>()
    val actions = _actions.observable

    private val _state = MutableLiveData<State>()
    val state = _state as LiveData<State>

    sealed class Action {
        object LaunchScoreActivity : Action()
        object ShowError : Action()
    }

    sealed class State {
        object FindingLocation : State()
        object Ready : State()
    }

    fun setupHome() = viewModelScope.launch {
        _state.postValue(FindingLocation)
        val location = LocationHelper.fetchCurrentLocation()
        if (location != null) {
            Prefs.userLocation = location
            GeofenceHelper.clearGeofences()
            GeofenceHelper.addGeofence(location)
            _state.postValue(State.Ready)
            _actions.send(LaunchScoreActivity)
        } else {
            _state.postValue(State.Ready)
            _actions.send(ShowError)
        }
    }
}