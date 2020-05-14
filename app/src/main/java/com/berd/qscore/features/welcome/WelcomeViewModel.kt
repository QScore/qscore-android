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
        object LaunchScoreActivity : Action()
        object ShowLocationError : Action()
        class SetProgressDialogVisible(val visible: Boolean) : Action()
    }

    @Parcelize
    data class State(
        val progressVisible: Boolean = false
    ) : Parcelable

    override fun updateState(action: Action, state: State) =
        when (action) {
            is SetProgressDialogVisible -> state.copy(progressVisible = true)
            else -> state
        }

    override fun getInitialState() = State()

    suspend fun onContinue() {
        action(SetProgressDialogVisible(true))
        val location = LocationHelper.fetchCurrentLocation()
        action(SetProgressDialogVisible(false))
        if (location != null) {
            Prefs.userLocation = location
            GeofenceHelper.setGeofence(location)
            action(LaunchScoreActivity)
        } else {
            action(ShowLocationError)
        }
    }

    fun continueWithoutLocation() = viewModelScope.launch {
        //Set status to AWAY
        GeofenceHelper.clearGeofences()
        Api.createGeofenceEvent(GeofenceEventType.AWAY)
        action(LaunchScoreActivity)
    }
}
