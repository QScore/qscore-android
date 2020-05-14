package com.berd.qscore.features.settings

import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.settings.SettingsViewModel.SettingsAction.LaunchLoginActivity
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.launch

class SettingsViewModel : RxViewModel<SettingsViewModel.SettingsAction>() {


    sealed class SettingsAction {
        object LaunchLoginActivity : SettingsAction()
    }

    fun onLogOut() = viewModelScope.launch {
        LoginManager.logout()
        action(LaunchLoginActivity)
    }
}
