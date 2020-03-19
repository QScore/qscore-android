package com.berd.qscore.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.splash.Action.LaunchLoginActivity
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.rx.RxEventSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


sealed class Action {
    object LaunchLoginActivity : Action()
    object LaunchWelcomeActivity : Action()
    object LaunchScoreActivity : Action()
}

class SplashViewModel : ViewModel() {
    val appContext = Injector.appContext

    private val _events = RxEventSender<Action>()
    val events = _events.observable

    fun onCreate() = viewModelScope.launch {
        delay(1500)
        _events.send(LaunchLoginActivity)
    }

    fun checkLoggedIn() {

    }
}