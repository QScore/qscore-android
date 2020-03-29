package com.berd.qscore.features.login.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.login.LoginManager.SignupEvent.NeedConfirmation
import com.berd.qscore.features.login.LoginManager.SignupEvent.Success
import com.berd.qscore.features.login.confirmation.ConfirmViewModel.Action.*
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.rx.RxEventSender
import kotlinx.coroutines.launch

class ConfirmViewModel : ViewModel() {

    private val _actions = RxEventSender<Action>()
    val actions = _actions.observable

    sealed class Action {
        object LaunchScoreActivity : Action()
        object LaunchWelcomeActivity : Action()
        object ShowError : Action()
        object ShowCodeToast : Action()
    }

    fun onConfirm(username: String, code: String) = viewModelScope.launch {
        when (LoginManager.completeSignUp(username, code)) {
            Success -> handleSuccess()
            is NeedConfirmation -> handleNeedConfirmation(username)
        }
    }

    private suspend fun handleNeedConfirmation(username: String) {
        LoginManager.sendConfirmationCode(username)
        _actions.send(ShowError)
    }

    private fun handleSuccess() {
        if (Prefs.userLocation != null) {
            _actions.send(LaunchScoreActivity)
        } else {
            _actions.send(LaunchWelcomeActivity)
        }
    }

    fun onResend(username: String) = viewModelScope.launch {
        LoginManager.sendConfirmationCode(username)
        _actions.send(ShowCodeToast)
    }
}