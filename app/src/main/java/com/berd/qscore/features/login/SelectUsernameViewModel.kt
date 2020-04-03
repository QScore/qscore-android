package com.berd.qscore.features.login

import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.SelectUsernameViewModel.Action
import com.berd.qscore.features.login.SelectUsernameViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.login.SelectUsernameViewModel.Action.LaunchWelcomeActivity
import com.berd.qscore.features.login.SelectUsernameViewModel.State
import com.berd.qscore.features.login.SelectUsernameViewModel.State.*
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.type.UpdateUserInfoInput
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectUsernameViewModel : RxViewModel<Action, State>() {

    sealed class Action {
        object LaunchScoreActivity : Action()
        object LaunchWelcomeActivity : Action()
    }

    sealed class State {
        object InProgress : State()
        object CheckingUsername : State()
        object ContinueError : State()
        object Ready : State()
        class FieldsUpdated(
            val usernameError: Boolean,
            val signUpIsReady: Boolean
        ) : State()
    }

    fun onContinue(username: String) = viewModelScope.launch {
        state = InProgress
        val input = UpdateUserInfoInput(username = username)
        try {
            Api.updateUserInfo(input)
            handleSuccess()
        } catch (e: Error) {
            Timber.d("Unable to update username: $e")
            //TODO: show error
        }
    }

    private fun handleError(error: Exception?) {
        Timber.d("Unable to log in : $error")
        state = ContinueError
    }

    fun onFieldsUpdated(username: String) = viewModelScope.launch {
        state = CheckingUsername

        val usernameError = false
        val signUpIsReady = !usernameError

        state = FieldsUpdated(usernameError, signUpIsReady)
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