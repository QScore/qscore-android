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
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel : RxViewModel<Action, State>() {

    sealed class Action {
        object LaunchScoreActivity : Action()
        object LaunchWelcomeActivity : Action()
        object LaunchUsernameActivity : Action()
    }

    sealed class State {
        object InProgress : State()
        object ResetInProgress : State()
        object LoginError : State()
        object ResetError : State()
        object Ready : State()
        object PasswordReset : State()
        class FieldsUpdated(
            val emailError: Boolean,
            val passwordError: Boolean,
            val signUpIsReady: Boolean
        ) : State()
    }

    fun onLogin(email: String, password: String) = viewModelScope.launch {
        state = InProgress
        when (val result = LoginManager.login(email, password)) {
            is Success -> handleSuccess(email)
            is Error -> handleError(result.error)
        }
    }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        state = InProgress
        try {
            when (val result = LoginManager.loginFacebook(supportFragmentManager)) {
                is Success -> handleSuccess("")
                is Error -> handleError(result.error)
            }
        } catch (e: CancellationException) {
            state = Ready
        }
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        state = ResetInProgress
        when (val result = LoginManager.sendPasswordResetEmail(email)) {
            is Success -> handleReset()
            is Error -> handleResetError(result.error)
        }
    }

    fun onFieldsUpdated(email: String, password: String) = viewModelScope.launch {
        val matcher = LoginManager.emailPattern.matcher(email)
        val emailError = !matcher.matches() || email.isEmpty()

        val passwordError = (password.length < 6)

        val signUpIsReady =
            !emailError && !passwordError && email.isNotEmpty() && password.isNotEmpty()

        state = FieldsUpdated(emailError, passwordError, signUpIsReady)
    }

    private fun handleReset() {
        state = PasswordReset
    }

    private suspend fun handleSuccess(email: String) {
        state = Ready
        Prefs.userEmail = email
        UserRepository.loadCurrentUser()
        if (UserRepository.currentUser?.username.isNullOrEmpty()) {
            action(LaunchUsernameActivity)
        } else if (Prefs.userLocation != null) {
            action(LaunchScoreActivity)
        } else {
            action(LaunchWelcomeActivity)
        }
    }

    private fun handleError(error: Exception?) {
        Timber.d("Unable to log in: $error")
        state = LoginError
    }

    private fun handleResetError(error: Exception?) {
        Timber.d("Unable to reset password: $error")
        state = ResetError
    }
}
