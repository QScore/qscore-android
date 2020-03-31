package com.berd.qscore.features.login

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager.AuthEvent.Error
import com.berd.qscore.features.login.LoginManager.AuthEvent.Success
import com.berd.qscore.features.login.LoginViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.login.LoginViewModel.Action.LaunchWelcomeActivity
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.utils.rx.RxEventSender
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
        _state.postValue(State.InProgress)
        when (val result = LoginManager.login(username, password)) {
            is Success -> handleSuccess()
            is Error -> handleError(result.error)
        }
    }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        _state.postValue(State.InProgress)
        when (val result = LoginManager.loginFacebook(supportFragmentManager)) {
            is Success -> handleSuccess()
            is Error -> handleError(result.error)
        }
    }

    private fun handleSuccess() {
        _state.postValue(State.Ready)
        if (Prefs.userLocation != null) {
            _actions.send(LaunchScoreActivity)
        } else {
            _actions.send(LaunchWelcomeActivity)
        }
    }

    private fun handleError(error: Exception?) {
        _state.postValue(State.LoginError)
    }
}