package com.berd.qscore.features.login

import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.R
import com.berd.qscore.features.login.LoginManager.AuthEvent.Error
import com.berd.qscore.features.login.LoginManager.AuthEvent.Success
import com.berd.qscore.features.login.LoginViewModel.LoginAction
import com.berd.qscore.features.login.LoginViewModel.LoginAction.*
import com.berd.qscore.features.login.LoginViewModel.State
import com.berd.qscore.features.login.LoginViewModel.State.ToggleState
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.features.shared.viewmodel.StateAction
import com.berd.qscore.features.shared.viewmodel.StateMutation
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(private val handle: SavedStateHandle) : RxViewModelWithState<LoginAction, State>(handle) {

    @Parcelize
    data class State(
        val progressVisible: Boolean = false,
        val errorResId: Int? = null,
        val toggleState: ToggleState = ToggleState.SIGNUP,
        val loginEnabled: Boolean = false
    ) : Parcelable {
        enum class ToggleState {
            SIGNUP,
            LOGIN
        }
    }

    sealed class LoginAction(mutation: StateMutation<State>? = null) : StateAction<State>(mutation) {
        object LaunchScoreActivity : LoginAction()
        object LaunchWelcomeActivity : LoginAction()
        object LaunchUsernameActivity : LoginAction()
        object LaunchResetPasswordActivity : LoginAction()
        object LaunchPasswordActivity : LoginAction()
        class SetInitialState(val state: State) : LoginAction()

        object TransformToLogin : LoginAction({
            copy(toggleState = ToggleState.LOGIN)
        })

        object TransformToSignup : LoginAction({
            copy(toggleState = ToggleState.SIGNUP)
        })

        class SetProgressVisible(val visible: Boolean) : LoginAction({
            copy(progressVisible = visible)
        })

        class SetLoginButtonEnabled(val enabled: Boolean) : LoginAction({
            copy(loginEnabled = enabled)
        })

        class ShowLoginError(val resId: Int) : LoginAction({
            copy(errorResId = resId)
        })
    }

    fun onCreate() {
        //Setup initial
        action(SetInitialState(state))
    }

    override fun getInitialState() = State()

    fun onLogin(email: String, password: String) = viewModelScope.launch {
        action(SetProgressVisible(true))
        if (state.toggleState == ToggleState.LOGIN) {
            when (val result = LoginManager.login(email, password)) {
                is Success -> handleLoginSuccess(email)
                is Error -> handleError(result.error)
            }
        } else {
            when (val result = LoginManager.signUp(email, password)) {
                is Success -> action(LaunchPasswordActivity)
                is Error -> handleError(result.error)
            }
        }
        action(SetProgressVisible(false))
    }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        action(SetProgressVisible(true))
        try {
            when (val result = LoginManager.loginFacebook(supportFragmentManager)) {
                is Success -> handleLoginSuccess("")
                is Error -> handleError(result.error)
            }
        } catch (e: CancellationException) {
            action(SetProgressVisible(false))
        }
    }

    fun loginGoogle(activity: FragmentActivity) = viewModelScope.launch {
        action(SetProgressVisible(true))
        try {
            when (val result = LoginManager.loginGoogle(activity)) {
                is Success -> handleLoginSuccess("")
                is Error -> handleError(result.error)
            }
        } catch (e: CancellationException) {
            action(SetProgressVisible(false))
        }
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        action(LaunchResetPasswordActivity)
    }

    fun onFieldsUpdated(email: String, password: String) = viewModelScope.launch {
        val matcher = LoginManager.emailPattern.matcher(email)
        val emailError = !matcher.matches() || email.isEmpty()
        val passwordError = password.isEmpty()
        val loginEnabled = !emailError && !passwordError && email.isNotEmpty() && password.isNotEmpty()
        action(SetLoginButtonEnabled(loginEnabled))
    }

    private suspend fun handleLoginSuccess(email: String) {
        Prefs.userEmail = email
        try {
            val currentUser = UserRepository.getCurrentUser()
            action(SetProgressVisible(false))
            when {
                currentUser.username.isNullOrEmpty() -> action(LaunchUsernameActivity)
                Prefs.userLocation != null -> action(LaunchScoreActivity)
                else -> action(LaunchWelcomeActivity)
            }
        } catch (e: ApolloException) {
            Timber.d("Unable to get current user $e")
            action(SetProgressVisible(false))
            action(ShowLoginError(R.string.login_error))
        }
    }

    private fun handleError(error: Exception?) {
        Timber.d("Unable to log in: $error")
        action(ShowLoginError(R.string.login_error))
    }

    fun signUpToggleClicked() {
        if (state.toggleState == ToggleState.SIGNUP) {
            action(TransformToLogin)
            ToggleState.LOGIN
        } else {
            action(TransformToSignup)
            ToggleState.SIGNUP
        }
    }
}
