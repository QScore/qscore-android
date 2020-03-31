package com.berd.qscore.features.login

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager.AuthEvent.Error
import com.berd.qscore.features.login.LoginManager.AuthEvent.Success
import com.berd.qscore.features.login.LoginViewModel.Action
import com.berd.qscore.features.login.LoginViewModel.Action.*
import com.berd.qscore.features.login.LoginViewModel.State
import com.berd.qscore.features.login.LoginViewModel.State.*
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel : RxViewModel<Action, State>() {

    sealed class Action {
        object LaunchScoreActivity : Action()
        object LaunchWelcomeActivity : Action()
    }

    sealed class State {
        object InProgress : State()
        object ResetInProgress : State()
        object LoginError : State()
        object Ready : State()
        object PasswordReset : State()
    }

    fun onLogin(email: String, password: String) = viewModelScope.launch {
        state = InProgress
        when (val result = LoginManager.login(email, password)) {
            is Success -> handleSuccess()
            is Error -> handleError(result.error)
        }
    }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        state = InProgress
        when (val result = LoginManager.loginFacebook(supportFragmentManager)) {
            is Success -> handleSuccess()
            is Error -> handleError(result.error)
        }
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        state = ResetInProgress
        when (val result = LoginManager.sendPasswordResetEmail(email)) {
            is Success -> handleReset()
            is Error -> handleError(result.error)
        }
    }

    private fun handleReset(){
        state = PasswordReset
    }

    private fun handleSuccess() {
        state = Ready
        if (Prefs.userLocation != null) {
            action(LaunchScoreActivity)
        } else {
            action(LaunchWelcomeActivity)
        }
    }

    private fun handleError(error: Exception?) {
        Timber.d("Unable to log in: $error")
        state = LoginError
    }
}