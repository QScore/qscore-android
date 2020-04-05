package com.berd.qscore.features.friends

import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.features.friends.AddFriendsViewModel.Action
import com.berd.qscore.features.friends.AddFriendsViewModel.Action.*
import com.berd.qscore.features.friends.AddFriendsViewModel.State
import com.berd.qscore.features.friends.AddFriendsViewModel.State.*
import kotlinx.coroutines.launch
import timber.log.Timber

class AddFriendsViewModel : RxViewModel<Action, State>() {

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
        handleSuccess()
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