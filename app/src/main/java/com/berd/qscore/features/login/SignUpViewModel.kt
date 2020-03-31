package com.berd.qscore.features.login

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager.AuthEvent
import com.berd.qscore.features.login.SignUpViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.login.SignUpViewModel.Action.LaunchWelcomeActivity
import com.berd.qscore.features.login.SignUpViewModel.State.*
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.rx.RxEventSender
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern

class SignUpViewModel : ViewModel() {

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

    private val _actions = RxEventSender<Action>()
    val actions = _actions.observable

    private val _state = MutableLiveData<State>()
    val state = _state as LiveData<State>

    private val emailPattern: Pattern by lazy {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    }

    fun onSignUp(username: String, email: String, password: String) = viewModelScope.launch {
        _state.postValue(InProgress)
        when (val result = LoginManager.signUp(email, password)) {
            is AuthEvent.Success -> handleSuccess()
            is AuthEvent.Error -> handleError(result.error)
        }
    }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        _state.postValue(InProgress)
        try {
            when (val result = LoginManager.loginFacebook(supportFragmentManager)) {
                is LoginManager.AuthEvent.Success -> handleSuccess()
                is LoginManager.AuthEvent.Error -> handleError(result.error)
            }
        } catch (e: CancellationException) {
            _state.postValue(Ready)
        }
    }

    private fun handleError(error: Exception?) {
        Timber.d("Unable to log in : $error")
        _state.postValue(SignUpError)
    }

    fun onFieldsUpdated(username: String, email: String, password: String) = viewModelScope.launch {
        val usernameError = false

        val matcher = emailPattern.matcher(email)
        val emailError = !matcher.matches() && email.isNotEmpty()

        val passwordError = (password.length < 8)

        val signUpIsReady =
            !usernameError && !emailError && !passwordError && username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()

        _state.postValue(FieldsUpdated(usernameError, emailError, passwordError, signUpIsReady))
    }

    private fun handleSuccess() {
        _state.postValue(Ready)
        if (Prefs.userLocation != null) {
            _actions.send(LaunchScoreActivity)
        } else {
            _actions.send(LaunchWelcomeActivity)
        }
    }
}