package com.berd.qscore.features.welcome

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.*
import com.berd.qscore.type.GeofenceEventType
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch

class WelcomeViewModel(handle: SavedStateHandle) : RxViewModelWithState<WelcomeViewModel.Action, WelcomeViewModel.State>(handle) {

    sealed class Action {
        class Initialize(val state: State) : Action()
        object LaunchScoreActivity : Action()
        object ShowLocationError : Action()
        class SetProgressVisible(val visible: Boolean) : Action()
    }

    @Parcelize
    data class State(
        val progressVisible: Boolean = false
    ) : Parcelable

    override fun updateState(action: Action, state: State) =
        when (action) {
            is SetProgressVisible -> state.copy(progressVisible = action.visible)
            else -> state
        }

    fun onCreate() {
        action(Initialize(state))
    }

    override fun getInitialState() = State()

    suspend fun onContinue() {
        action(SetProgressVisible(true))
        val location = LocationHelper.fetchCurrentLocation()
        action(SetProgressVisible(false))
        if (location != null) {
            Prefs.userLocation = location
            GeofenceHelper.setGeofence(location)
            action(LaunchScoreActivity)
        } else {
            action(ShowLocationError)
        }
    }

    fun continueWithoutLocation() = viewModelScope.launch {
        action(SetProgressVisible(true))
        //Set status to AWAY
        GeofenceHelper.clearGeofences()
        Api.createGeofenceEvent(GeofenceEventType.AWAY)
        action(SetProgressVisible(false))
        action(LaunchScoreActivity)
    }
}
