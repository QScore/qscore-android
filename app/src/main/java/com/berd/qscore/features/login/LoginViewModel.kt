package com.berd.qscore.features.login

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager.LoginEvent.Success
import com.berd.qscore.features.login.LoginManager.LoginEvent.Unknown
import com.berd.qscore.features.login.LoginViewModel.Action.*
import com.berd.qscore.features.login.LoginViewModel.State.*
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.rx.RxEventSender
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    sealed class Action {
        object LaunchScoreActivity : Action()
        object LaunchWelcomeActivity : Action()
    }

    sealed class State {
        object InProgress : State()
        object LoginError : State()
        object Ready : State()
    }

    private val _actions = RxEventSender<Action>()
    val actions = _actions.observable

    private val _state = MutableLiveData<State>()
    val state = _state as LiveData<State>

    fun onLogin(username: String, password: String) = viewModelScope.launch {
        _state.postValue(InProgress)
        try {
            when (LoginManager.login(username, password)) {
                Success -> handleSuccess()
                Unknown -> handleUnknown()
            }
        } catch (e: Exception) {
            _state.postValue(LoginError)
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
                _state.postValue(LoginError)
            }
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
        _state.postValue(LoginError)
    }
}