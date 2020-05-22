package com.berd.qscore.features.username

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.R
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.features.username.UsernameViewModel.UsernameAction.*
import com.berd.qscore.utils.injection.Injector
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class UsernameViewModel(
    handle: SavedStateHandle,
    private val shouldLaunchWelcome: Boolean,
    private val isNewUser: Boolean
) :
    RxViewModelWithState<UsernameViewModel.UsernameAction, UsernameViewModel.UsernameState>(handle) {

    private var job: Job? = null
    private val userRepository = Injector.userRepository

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
            val validUsername = username.length >= 4
            try {
                if (!validUsername) {
                    action(SetContinueProgressVisible(false))
                    action(SetHint(R.string.username_must_be_at_least_4_characters, R.color.text))
                    action(SetContinueEnabled(false))
                } else {
                    action(SetContinueProgressVisible(true))
                    val exists = userRepository.checkUsernameExists(username)
                    action(SetContinueProgressVisible(false))
                    if (exists) {
                        action(SetHint(R.string.username_exists, R.color.punch_red))
                        action(SetContinueEnabled(false))
                    } else {
                        action(SetHint(R.string.space, R.color.text))
                        action(SetContinueEnabled(true))
                    }
                }
            } catch (e: ApolloException) {
                action(SetContinueProgressVisible(false))
                action(SetHint(R.string.error_checking_username, R.color.punch_red))
                Timber.d("Unable to check if username exists for users: $e")
            }
        }
    }

    fun onContinue(username: String) {
        viewModelScope.launch {
            action(SetContinueProgressVisible(true))
            action(SetFieldEnabled(false))
            try {
                if (isNewUser) {
                    userRepository.createUser(username)
                } else {
                    userRepository.updateUsername(username)
                }
                if (shouldLaunchWelcome) {
                    action(LaunchWelcomeActivity)
                } else {
                    action(ShowSuccessToast)
                    action(FinishActivity)
                }
            } catch (e: ApolloException) {
                action(SetHint(R.string.unable_to_continue_please_try_again_later, R.color.punch_red))
            }
            action(SetContinueProgressVisible(false))
            action(SetFieldEnabled(true))
        }
    }
}
