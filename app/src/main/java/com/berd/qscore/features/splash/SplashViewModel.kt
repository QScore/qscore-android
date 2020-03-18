package com.berd.qscore.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.splash.Action.LaunchScoreActivity
import com.berd.qscore.features.splash.Action.LaunchWelcomeActivity
import com.berd.qscore.utils.rx.RxEventSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Action {
    object LaunchWelcomeActivity : Action()
    object LaunchScoreActivity : Action()
}

class SplashViewModel() : ViewModel() {
    private val _events = RxEventSender<Action>()
    val events = _events.observable

    fun onCreate() = viewModelScope.launch {
        delay(1500)
        if (Prefs.userLocation == null) {
            _events.send(LaunchWelcomeActivity)
        } else {
            _events.send(LaunchScoreActivity)
        }
    }


}