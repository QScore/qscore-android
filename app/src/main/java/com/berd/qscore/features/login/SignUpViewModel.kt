package com.berd.qscore.features.login

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager.AuthEvent
import com.berd.qscore.features.login.SignUpViewModel.Action
import com.berd.qscore.features.login.SignUpViewModel.Action.*
import com.berd.qscore.features.login.SignUpViewModel.State
import com.berd.qscore.features.login.SignUpViewModel.State.*
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

class SignUpViewModel : RxViewModel<Action, State>() {

    sealed class Action {
        object LaunchUsernameActivity : Action()
        object LaunchScoreActivity : Action()
        object LaunchWelcomeActivity : Action()
    }

    sealed class State {
        object InProgress : State()
        object SignUpError : State()
        object Ready : State()
        class FieldsUpdated(
            val emailError: Boolean,
            val passwordError: Boolean,
            val signUpIsReady: Boolean
        ) : State()
    }

    fun onSignUp(email: String, password: String) = viewModelScope.launch {
        state = InProgress
        when (val result = LoginManager.signUp(email, password)) {
            is AuthEvent.Success -> handleSuccess(email)
            is AuthEvent.Error -> handleError(result.error)
        }
    }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        state = InProgress
        try {
            when (val result = LoginManager.loginFacebook(supportFragmentManager)) {
                is AuthEvent.Success -> handleSuccess("")
                is AuthEvent.Error -> handleError(result.error)
            }
        } catch (e: CancellationException) {
            state = Ready
        }
    }

    private fun handleError(error: Exception?) {
        Timber.d("Unable to log in : $error")
        state = SignUpError
    }

    fun onFieldsUpdated(email: String, password: String) = viewModelScope.launch {
        val matcher = LoginManager.emailPattern.matcher(email)
        val emailError = !matcher.matches() || email.isEmpty()

        val passwordError = (password.length < 6)

        val signUpIsReady =
            !emailError && !passwordError && email.isNotEmpty() && password.isNotEmpty()

        state = FieldsUpdated(emailError, passwordError, signUpIsReady)
    }

    private suspend fun handleSuccess(email: String) {
        state = Ready
        Prefs.userEmail = email
        if (!LoginManager.checkUserHasUsername()) {
            action(LaunchUsernameActivity)
        } else if (Prefs.userLocation != null) {
            action(LaunchScoreActivity)
        } else {
            action(LaunchWelcomeActivity)
        }
    }
}