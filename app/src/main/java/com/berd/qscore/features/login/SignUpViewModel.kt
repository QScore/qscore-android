package com.berd.qscore.features.login

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException
import com.berd.qscore.features.login.LoginManager.LoginEvent.Success
import com.berd.qscore.features.login.LoginManager.LoginEvent.Unknown
import com.berd.qscore.features.login.LoginManager.SignupEvent
import com.berd.qscore.features.login.SignUpViewModel.Action.*
import com.berd.qscore.features.login.SignUpViewModel.State.*
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.rx.RxEventSender
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SignUpViewModel : ViewModel() {

    sealed class Action {
        object LaunchScoreActivity : Action()
        class LaunchConfirmActivity(val email: String) : Action()
        object LaunchWelcomeActivity : Action()
    }

    sealed class State {
        object InProgress : State()
        object SignUpError : State()
        object Ready : State()
        class UsernameChange(val usernameError: Boolean, val signUpIsReady: Boolean) : State()
        class EmailChange(val emailError: Boolean, val signUpIsReady: Boolean) : State()
        class PasswordChange(val passwordError: Boolean, val signUpIsReady: Boolean) : State()
    }

    private val _actions = RxEventSender<Action>()
    val actions = _actions.observable

    private val _state = MutableLiveData<State>()
    val state = _state as LiveData<State>

    fun onSignUp(username: String, email: String, password: String) = viewModelScope.launch {
        _state.postValue(InProgress)
        try {
            when (LoginManager.signUp(username, email, password)) {
                SignupEvent.Success -> handleSuccess()
                is SignupEvent.NeedConfirmation -> handleNeedConfirmation(email)
            }
        } catch (e: Exception) {
            when (e) {
                is UserNotConfirmedException -> {
                    _state.postValue(Ready)
                    _actions.send(LaunchConfirmActivity(email))
                }
                else -> _state.postValue(SignUpError)
            }
        }
    }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        _state.postValue(InProgress)
        try {
            when (LoginManager.loginFacebook(supportFragmentManager)) {
                Success -> handleSuccess()
                Unknown -> handleUnknown()
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                _state.postValue(Ready)
            } else {
                _state.postValue(SignUpError)
            }
        }
    }

    fun checkUsername(username: String, emailReady: Boolean, passwordReady: Boolean) = viewModelScope.launch {
        val usernameReady = username.isNotEmpty();     //TODO more checks on username availability

        if (usernameReady && emailReady && passwordReady){
            _state.postValue(UsernameChange(!usernameReady,true))
        } else {
            _state.postValue(UsernameChange(!usernameReady,false))
        }
    }

    fun checkEmail(usernameReady: Boolean, email: String, passwordReady: Boolean) = viewModelScope.launch {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email);
        val emailReady = matcher.matches();

        if (usernameReady && emailReady && passwordReady){
            _state.postValue(EmailChange(!emailReady,true))
        } else {
            _state.postValue(EmailChange(!emailReady,false))
        }
    }

    fun checkPassword(usernameReady: Boolean, emailReady: Boolean, password: String) = viewModelScope.launch {
        val passwordReady = (password.length >= 8)

        if (usernameReady && emailReady && passwordReady){
            _state.postValue(PasswordChange(!passwordReady,true))
        } else {
            _state.postValue(PasswordChange(!passwordReady,false))
        }
    }

    private fun handleSuccess() {
        _state.postValue(Ready)
        if (Prefs.userLocation != null) {
            _actions.send(LaunchScoreActivity)
        } else {
            _actions.send(LaunchWelcomeActivity)
        }
    }

    private fun handleUnknown() {
        _state.postValue(SignUpError)
    }

    private fun handleNeedConfirmation(email: String) {
        _state.postValue(Ready)
        _actions.send(LaunchConfirmActivity(email))
    }
}