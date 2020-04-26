package com.berd.qscore.features.main

import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.main.MainViewModel.MainAction
import com.berd.qscore.features.main.MainViewModel.MainAction.LaunchLoginActivity
import com.berd.qscore.features.main.MainViewModel.MainState
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.launch

class MainViewModel : RxViewModel<MainAction, MainState>() {

    sealed class MainAction {
        object LaunchLoginActivity : MainAction()
    }

    sealed class MainState {

    }

    fun onLogout() = viewModelScope.launch {
        LoginManager.logout()
        action(LaunchLoginActivity)
    }
}
