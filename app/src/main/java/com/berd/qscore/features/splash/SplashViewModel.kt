package com.berd.qscore.features.splash

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.features.splash.Action.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


sealed class Action {
    object LaunchSignUpActivity : Action()
    object LaunchLoginActivity : Action()
    object LaunchWelcomeActivity : Action()
    object LaunchScoreActivity : Action()
    object LaunchUsernameActivity : Action()
}

class SplashViewModel : RxViewModel<Action, Unit>() {
    fun onCreate() = viewModelScope.launch {
        try {
            delay(1500)
            if (LoginManager.isLoggedIn) {
                UserRepository.getCurrentUser()
                if (!UserRepository.currentUser?.username.isNullOrEmpty()) {
                    if (Prefs.userLocation != null) {
                        action(LaunchScoreActivity)
                    } else {
                        action(LaunchWelcomeActivity)
                    }
                } else {
                    action(LaunchUsernameActivity)
                }
            } else if (Prefs.userEmail.isNotEmpty()) {
                action(LaunchLoginActivity)
            } else {
                action(LaunchSignUpActivity)
            }
        } catch (e: ApolloException) {
            Timber.d("Unable to load current user: $e")
            action(LaunchSignUpActivity)
        }
    }
}

