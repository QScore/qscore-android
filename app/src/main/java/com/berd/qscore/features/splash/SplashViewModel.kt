package com.berd.qscore.features.splash

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.features.shared.viewmodel.RxViewModelOld
import com.berd.qscore.features.splash.Action.*
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


sealed class Action {
    object LaunchLoginActivity : Action()
    object LaunchWelcomeActivity : Action()
    object LaunchScoreActivity : Action()
    class LaunchUsernameActivity(val isNewUser: Boolean) : Action()
}

class SplashViewModel : RxViewModel<Action>() {
    private val userRepository = Injector.userRepository
    private val locationHelper = Injector.locationHelper

    fun onCreate() = viewModelScope.launch {
        delay(200)
        if (LoginManager.isLoggedIn) {
            try {
                userRepository.getCurrentUser()
                if (Prefs.userLocation != null && locationHelper.hasAllPermissions) {
                    action(LaunchScoreActivity)
                } else {
                    action(LaunchWelcomeActivity)
                }
            } catch (e: ApolloException) {
                //Need to create a new user
                action(LaunchUsernameActivity(isNewUser = true))
                return@launch
            }
        } else {
            action(LaunchLoginActivity)
        }
    }
}

