package com.berd.qscore.features.login

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager.AuthEvent.Error
import com.berd.qscore.features.login.LoginManager.AuthEvent.Success
import com.berd.qscore.features.login.LoginViewModel.Action
import com.berd.qscore.features.login.LoginViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.login.LoginViewModel.Action.LaunchWelcomeActivity
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
        object LaunchForgotActivity : Action()
    }

    sealed class State {
        object InProgress : State()
        object LoginError : State()
        object Ready : State()
    }

    fun onLogin(username: String, password: String) = viewModelScope.launch {
        state = InProgress
        when (val result = LoginManager.login(username, password)) {
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

    fun onForgotPassword(username: String) = viewModelScope.launch {
        _state.postValue(InProgress)
        try {
            when (LoginManager.forgotPassword(username)) {
                LoginManager.ForgotPasswordEvent.Done -> handleReset()
                LoginManager.ForgotPasswordEvent.ConfirmationCode -> handleReset()
                LoginManager.ForgotPasswordEvent.Unknown -> handleUnknown()
            }
        } catch (e: Exception) {
            _state.postValue(LoginError)
        }
    }

    private fun handleReset(){
        _state.postValue(Ready)
        _actions.send(LaunchForgotActivity)
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