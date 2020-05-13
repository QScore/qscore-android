package com.berd.qscore.features.username

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.username.UsernameViewModel.Action
import com.berd.qscore.features.username.UsernameViewModel.Action.*
import com.berd.qscore.features.username.UsernameViewModel.State
import com.berd.qscore.features.username.UsernameViewModel.State.*
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class UsernameViewModel : RxViewModel<Action, State>() {

    sealed class Action {
        object LaunchScoreActivity : Action()
        object LaunchWelcomeActivity : Action()
        object ReturnToLogIn : Action()
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
        try {
            Api.createUser(username)
            handleSuccess()
        } catch (e: ApolloException) {
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

    fun onBackPressed() {
        LoginManager.logout()
        action(ReturnToLogIn)
    }
}
