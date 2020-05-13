package com.berd.qscore.features.username

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloHttpException
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch

class UsernameViewModel(handle: SavedStateHandle) :
    RxViewModelWithState<UsernameViewModel.UsernameAction, UsernameViewModel.UsernameState>(handle) {

    @Parcelize
    data class UsernameState(
        val continueEnabled: Boolean = false,
        val hasError: Boolean = false,
        val progressVisible: Boolean = false
    ) : Parcelable

    sealed class UsernameAction {
        class Initialize(val state: UsernameState) : UsernameAction()
        class SetContinueEnabled(val enabled: Boolean) : UsernameAction()
        object ShowError : UsernameAction()
        class SetProgressVisible(val visible: Boolean) : UsernameAction()
        object LaunchMainActivity : UsernameAction()
        object LaunchWelcomeActivity : UsernameAction()
    }

    override fun getInitialState() = UsernameState()

    override fun updateState(action: UsernameAction, state: UsernameState) =
        when (action) {
            is UsernameAction.SetContinueEnabled -> state.copy(continueEnabled = action.enabled)
            is UsernameAction.ShowError -> state.copy(hasError = true)
            is UsernameAction.SetProgressVisible -> state.copy(progressVisible = action.visible)
            else -> state
        }

    fun onPasswordChange(str: String) {
        action(UsernameAction.SetContinueEnabled(str.length >= 4))
    }

    fun onContinue(username: String) {
        viewModelScope.launch {
            action(UsernameAction.SetProgressVisible(true))
            try {
                UserRepository.updateUserInfo(username)
                action(UsernameAction.LaunchWelcomeActivity)
            } catch (e: ApolloHttpException) {
                action(UsernameAction.ShowError)
            }
            action(UsernameAction.SetProgressVisible(false))
        }
    }
}
