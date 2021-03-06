package com.berd.qscore.features.setpassword

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager
import com.berd.qscore.features.login.LoginManager.AuthEvent.Error
import com.berd.qscore.features.login.LoginManager.AuthEvent.Success
import com.berd.qscore.features.setpassword.PasswordViewModel.PasswordAction.*
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import timber.log.Timber

class PasswordViewModel(handle: SavedStateHandle) :
    RxViewModelWithState<PasswordViewModel.PasswordAction, PasswordViewModel.PasswordState>(handle) {

    @Parcelize
    data class PasswordState(
        val continueEnabled: Boolean = false,
        val hasError: Boolean = false,
        val progressVisible: Boolean = false,
        val errorMessage: String? = null
    ) : Parcelable

    sealed class PasswordAction {
        class Initialize(val state: PasswordState) : PasswordAction()
        class SetContinueEnabled(val enabled: Boolean) : PasswordAction()
        class ShowError(val message: String?) : PasswordAction()
        class SetProgressVisible(val visible: Boolean) : PasswordAction()
        object LaunchUsernameActivity : PasswordAction()
    }

    override fun getInitialState() = PasswordState()

    override fun updateState(action: PasswordAction, state: PasswordState) =
        when (action) {
            is SetContinueEnabled -> state.copy(continueEnabled = action.enabled)
            is ShowError -> state.copy(hasError = true, errorMessage = action.message)
            is SetProgressVisible -> state.copy(progressVisible = action.visible)
            else -> state
        }

    fun onPasswordChange(password: String) {
        action(SetContinueEnabled(password.length >= 6))
    }

    fun onContinue(email: String, password: String) {
        viewModelScope.launch {
            action(SetProgressVisible(true))
            val result = LoginManager.signUp(email, password)
            when (result) {
                Success -> {
                    Prefs.userLocation = null
                    action(LaunchUsernameActivity)
                }
                is Error -> {
                    val errorMessage = result.error?.message
                    action(ShowError(errorMessage))
                }
            }
            action(SetProgressVisible(false))
        }
    }
}
