package com.berd.qscore.features.login.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.login.LoginManager.SignupEvent.NeedConfirmation
import com.berd.qscore.features.login.LoginManager.SignupEvent.Success
import com.berd.qscore.features.login.confirmation.ConfirmViewModel.Action.LaunchWelcomeActivity
import com.berd.qscore.features.login.confirmation.ConfirmViewModel.Action.ShowError
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
    }

    fun onConfirm(email: String, code: String) = viewModelScope.launch {
        when (LoginManager.completeSignUp(email, code)) {
            Success -> handleSuccess()
            is NeedConfirmation -> handleNeedConfirmation(email)
        }
    }

    private suspend fun handleNeedConfirmation(email: String) {
        LoginManager.sendConfirmationCode(email)
        _actions.send(ShowError)
    }

    private fun handleSuccess() {
        if (Prefs.userLocation != null) {
            _actions.send(Action.LaunchScoreActivity)
        } else {
            _actions.send(LaunchWelcomeActivity)
        }
    }


}