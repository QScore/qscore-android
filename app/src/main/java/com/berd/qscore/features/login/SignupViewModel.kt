package com.berd.qscore.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException
import com.berd.qscore.features.login.LoginManager.SignupEvent.NeedConfirmation
import com.berd.qscore.features.login.LoginManager.SignupEvent.Success
import com.berd.qscore.utils.rx.RxEventSender
import kotlinx.coroutines.launch
import timber.log.Timber

class SignupViewModel : ViewModel() {

    private val _actions = RxEventSender<LoginViewModel.Action>()
    val actions = _actions.observable

    fun signup(email: String, password: String) = viewModelScope.launch {
        try {
            val result = LoginManager.signUp(email, password)
            when (result) {
                Success -> handleSuccess()
                is NeedConfirmation -> handleConfirmation(email)
            }
        } catch (e: Exception) {
            handleError(e, email)
        }
    }

    private suspend fun handleError(e: Exception, email: String) {
        if (e is UserNotConfirmedException) {
            LoginManager.sendConfirmationCode(email)
            _actions.send(LoginViewModel.Action.LaunchConfirmActivity(email))
        }
        _actions.send(LoginViewModel.Action.ShowError)
    }

    private suspend fun handleConfirmation(email: String) {
        Timber.d(">>Signup needs confirmation")
        LoginManager.sendConfirmationCode(email)
    }

    private fun handleSuccess() {
        Timber.d(">>Signup success")
    }

}