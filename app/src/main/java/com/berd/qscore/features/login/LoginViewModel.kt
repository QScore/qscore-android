package com.berd.qscore.features.login

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException
import com.berd.qscore.features.login.LoginManager.LoginEvent.Success
import com.berd.qscore.features.login.LoginManager.LoginEvent.Unknown
import com.berd.qscore.features.login.LoginViewModel.Action.*
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.rx.RxEventSender
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    sealed class Action {
        object LaunchScoreActivity : Action()
        class LaunchConfirmActivity(val email: String) : Action()
        object LaunchWelcomeActivity : Action()
        object ShowError : Action()
    }

    private val _actions = RxEventSender<Action>()
    val actions = _actions.observable

    fun onLogin(email: String, password: String) = viewModelScope.launch {
        try {
            when (LoginManager.login(email, password)) {
                Success -> handleSuccess()
                Unknown -> handleUnknown()
            }
        } catch (e: Exception) {
            handleError(e, email)
        }
    }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        try {
            when (LoginManager.loginFacebook(supportFragmentManager)) {
                Success -> handleSuccess()
                Unknown -> handleUnknown()
            }
        } catch (e: Exception) {
            _actions.send(ShowError)
        }
    }

    private fun handleSuccess() {
        if (Prefs.userLocation != null) {
            _actions.send(LaunchScoreActivity)
        } else {
            _actions.send(LaunchWelcomeActivity)
        }
    }

    private fun handleUnknown() {
        _actions.send(ShowError)
    }

    private suspend fun handleError(e: Exception, email: String) {
        if (e is UserNotConfirmedException) {
            LoginManager.sendConfirmationCode(email)
            _actions.send(LaunchConfirmActivity(email))
        }
        _actions.send(ShowError)
    }
}