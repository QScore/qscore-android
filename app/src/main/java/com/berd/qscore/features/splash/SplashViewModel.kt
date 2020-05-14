package com.berd.qscore.features.splash

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelOld
import com.berd.qscore.features.splash.Action.*
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


sealed class Action {
    object LaunchLoginActivity : Action()
    object LaunchWelcomeActivity : Action()
    object LaunchScoreActivity : Action()
    object LaunchUsernameActivity : Action()
}

class SplashViewModel : RxViewModelOld<Action, Unit>() {
    fun onCreate() = viewModelScope.launch {
        try {
            delay(1500)
            if (LoginManager.isLoggedIn) {
                UserRepository.getCurrentUser()
                if (!UserRepository.currentUser?.username.isNullOrEmpty()) {
                    if (Prefs.userLocation != null && LocationHelper.hasAllPermissions) {
                        action(LaunchScoreActivity)
                    } else {
                        action(LaunchWelcomeActivity)
                    }
                } else {
                    action(LaunchUsernameActivity)
                }
            } else {
                action(LaunchLoginActivity)
            }
        } catch (e: ApolloException) {
            Timber.d("Unable to load current user: $e")
            action(LaunchLoginActivity)
        }
    }
}

