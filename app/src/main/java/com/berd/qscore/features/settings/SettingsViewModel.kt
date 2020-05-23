package com.berd.qscore.features.settings

import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.geofence.GeofenceStatus
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.settings.SettingsViewModel.SettingsAction.LaunchLoginActivity
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.type.GeofenceEventType
import com.berd.qscore.utils.injection.Injector
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SettingsViewModel : RxViewModel<SettingsViewModel.SettingsAction>() {
    private val userRepository = Injector.userRepository

    sealed class SettingsAction {
        object LaunchLoginActivity : SettingsAction()
    }

    fun onLogOut() = viewModelScope.launch {
        action(LaunchLoginActivity)
        userRepository.createGeofenceEvent(GeofenceStatus.AWAY)
        LoginManager.logout()
    }
}
