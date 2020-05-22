package com.berd.qscore.features.welcome

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.geofence.GeofenceStatus
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.*
import com.berd.qscore.type.GeofenceEventType
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import timber.log.Timber

class WelcomeViewModel(handle: SavedStateHandle) : RxViewModelWithState<WelcomeViewModel.Action, WelcomeViewModel.State>(handle) {

    private val geofenceHelper = Injector.geofenceHelper
    private val userRepository = Injector.userRepository
    private val locationHelper = Injector.locationHelper

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
        try {
            val location = locationHelper.fetchCurrentLocation()
            if (location != null) {
                Prefs.userLocation = location
                geofenceHelper.setGeofence(location)
                userRepository.createGeofenceEvent(GeofenceStatus.HOME)
                action(LaunchScoreActivity)
            } else {
                action(ShowLocationError)
            }
            action(SetProgressVisible(false))
        } catch (e: IllegalStateException) {
            Timber.d("Unable to fetch location, permission not granted")
        }
    }

    fun continueWithoutLocation() = viewModelScope.launch {
        action(SetProgressVisible(true))
        //Set status to AWAY
        geofenceHelper.clearGeofences()
        userRepository.createGeofenceEvent(GeofenceStatus.AWAY)
        action(LaunchScoreActivity)
        action(SetProgressVisible(false))
    }
}
