package com.berd.qscore.resetpassword

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.login.LoginManager.AuthEvent.Error
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.resetpassword.ResetPasswordViewModel.ResetPasswordAction.*
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import timber.log.Timber

class ResetPasswordViewModel(handle: SavedStateHandle) :
    RxViewModelWithState<ResetPasswordViewModel.ResetPasswordAction, ResetPasswordViewModel.ResetPasswordState>(handle) {

    @Parcelize
    data class ResetPasswordState(
        val resetEnabled: Boolean = false,
        val showProgress: Boolean = false
    ) : Parcelable

    sealed class ResetPasswordAction {
        class Initialize(val state: ResetPasswordState) : ResetPasswordAction()
        class SetResetEnabled(val enabled: Boolean) : ResetPasswordAction()
        class SetProgressVisible(val visible: Boolean) : ResetPasswordAction()
        class FinishActivity(val email: String) : ResetPasswordAction()
    }

    fun onCreate() {
        action(Initialize(state))
    }

    override fun getInitialState() = ResetPasswordState()

    override fun updateState(action: ResetPasswordAction, state: ResetPasswordState) =
        when (action) {
            is SetResetEnabled -> state.copy(resetEnabled = action.enabled)
            is SetProgressVisible -> state.copy(showProgress = action.visible)
            else -> state
        }

    fun onEmailChange(email: String) {
        val isValid = LoginManager.isValidEmail(email)
        action(SetResetEnabled(isValid))
    }

    fun onReset(email: String) {
        viewModelScope.launch {
            action(SetProgressVisible(true))
            val result = LoginManager.sendPasswordResetEmail(email)
            when (result) {
                is Error -> {
                    val errorMessage = result.error?.message
                    Timber.e("Unable to reset password: $errorMessage")
                }
            }
            action(SetProgressVisible(false))
            action(FinishActivity(email))
        }
    }
}
