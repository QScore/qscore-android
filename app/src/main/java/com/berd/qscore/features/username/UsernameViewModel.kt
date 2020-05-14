package com.berd.qscore.features.username

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloHttpException
import com.berd.qscore.R
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.features.username.UsernameViewModel.UsernameAction.*
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UsernameViewModel(handle: SavedStateHandle, private val shouldLaunchWelcome: Boolean) :
    RxViewModelWithState<UsernameViewModel.UsernameAction, UsernameViewModel.UsernameState>(handle) {

    private var job: Job? = null

    @Parcelize
    data class UsernameState(
        val continueEnabled: Boolean = false,
        val hasError: Boolean = false,
        val continueProgressVisible: Boolean = false,
        val hintResId: Int = R.string.username_must_be_at_least_4_characters,
        val hintColorResId: Int = R.color.text
    ) : Parcelable

    sealed class UsernameAction {
        class Initialize(val state: UsernameState) : UsernameAction()
        class SetContinueEnabled(val enabled: Boolean) : UsernameAction()
        class SetHint(val resId: Int, val colorResId: Int) : UsernameAction()
        class SetContinueProgressVisible(val visible: Boolean) : UsernameAction()
        class SetFieldEnabled(val enabled: Boolean) : UsernameAction()
        object LaunchWelcomeActivity : UsernameAction()
        object FinishActivity : UsernameAction()
        object ShowSuccessToast : UsernameAction()
    }

    override fun getInitialState() = UsernameState()

    override fun updateState(action: UsernameAction, state: UsernameState) =
        when (action) {
            is SetContinueEnabled -> state.copy(continueEnabled = action.enabled)
            is SetContinueProgressVisible -> state.copy(continueProgressVisible = action.visible)
            is SetHint -> state.copy(hintResId = action.resId, hintColorResId = action.colorResId)
            else -> state
        }

    fun onUsernameChange(username: String) {
        job?.cancel()
        job = viewModelScope.launch {
            action(SetContinueProgressVisible(true))
            val exists = UserRepository.checkUsernameExists(username)
            action(SetContinueProgressVisible(false))
            val validUsername = username.length >= 4
            if (exists) {
                action(SetHint(R.string.username_exists, R.color.punch_red))
            } else if (!validUsername) {
                action(SetHint(R.string.username_must_be_at_least_4_characters, R.color.text))
            } else {
                action(SetHint(R.string.space, R.color.text))
            }
            val canContinue = !exists && validUsername
            action(SetContinueEnabled(canContinue))
        }
    }

    fun onContinue(username: String) {
        viewModelScope.launch {
            action(SetContinueProgressVisible(true))
            action(SetFieldEnabled(false))
            try {
                UserRepository.updateUserInfo(username)
                if (shouldLaunchWelcome) {
                    action(LaunchWelcomeActivity)
                } else {
                    action(ShowSuccessToast)
                    action(FinishActivity)
                }
            } catch (e: ApolloHttpException) {
                action(SetHint(R.string.unable_to_continue_please_try_again_later, R.color.punch_red))
            }
            action(SetContinueProgressVisible(false))
            action(SetFieldEnabled(true))
        }
    }
}
