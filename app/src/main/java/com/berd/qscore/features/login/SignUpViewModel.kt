package com.berd.qscore.features.login

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager.AuthEvent
import com.berd.qscore.features.login.SignUpViewModel.Action
import com.berd.qscore.features.login.SignUpViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.login.SignUpViewModel.Action.LaunchWelcomeActivity
import com.berd.qscore.features.login.SignUpViewModel.State
import com.berd.qscore.features.login.SignUpViewModel.State.*
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern

class SignUpViewModel : RxViewModel<Action, State>() {

    sealed class Action {
        object LaunchScoreActivity : Action()
        object LaunchWelcomeActivity : Action()
    }

    sealed class State {
        object InProgress : State()
        object SignUpError : State()
        object Ready : State()
        class FieldsUpdated(
            val usernameError: Boolean,
            val emailError: Boolean,
            val passwordError: Boolean,
            val signUpIsReady: Boolean
        ) : State()
    }

    private val emailPattern: Pattern by lazy {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    }

    fun onSignUp(username: String, email: String, password: String) = viewModelScope.launch {
        state = InProgress
        when (val result = LoginManager.signUp(email, password)) {
            is AuthEvent.Success -> handleSuccess()
            is AuthEvent.Error -> handleError(result.error)
        }
    }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        state = InProgress
        try {
            when (val result = LoginManager.loginFacebook(supportFragmentManager)) {
                is AuthEvent.Success -> handleSuccess()
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

    fun onFieldsUpdated(username: String, email: String, password: String) = viewModelScope.launch {
        val usernameError = false

        val matcher = emailPattern.matcher(email)
        val emailError = !matcher.matches() && email.isNotEmpty()

        val passwordError = (password.length < 8)

        val signUpIsReady =
            !usernameError && !emailError && !passwordError && username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()

        state = FieldsUpdated(usernameError, emailError, passwordError, signUpIsReady)
    }

    private fun handleSuccess() {
        state = Ready
        if (Prefs.userLocation != null) {
            action(LaunchScoreActivity)
        } else {
            action(LaunchWelcomeActivity)
        }
    }
}