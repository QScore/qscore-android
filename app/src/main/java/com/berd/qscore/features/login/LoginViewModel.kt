package com.berd.qscore.features.login

import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.R
import com.berd.qscore.features.geofence.GeofenceStatus
import com.berd.qscore.features.login.LoginManager.AuthEvent.Error
import com.berd.qscore.features.login.LoginManager.AuthEvent.Success
import com.berd.qscore.features.login.LoginViewModel.LoginAction
import com.berd.qscore.features.login.LoginViewModel.LoginAction.*
import com.berd.qscore.features.login.LoginViewModel.State
import com.berd.qscore.features.login.LoginViewModel.State.ToggleState
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.IllegalStateException

class LoginViewModel(handle: SavedStateHandle) : RxViewModelWithState<LoginAction, State>(handle) {
    private val userRepository by lazy { Injector.userRepository }
    private val geofenceHelper by lazy { Injector.geofenceHelper }
    private val locationHelper by lazy { Injector.locationHelper }

    sealed class LoginAction {
        object LaunchScoreActivity : LoginAction()
        object LaunchWelcomeActivity : LoginAction()
        class LaunchUsernameActivity(val isNewUser: Boolean) : LoginAction()
        class LaunchPasswordActivity(val email: String) : LoginAction()
        object TransformToLogin : LoginAction()
        object TransformToSignup : LoginAction()
        class SetProgressVisible(val visible: Boolean, val buttonResId: Int) : LoginAction()
        class SetLoginButtonEnabled(val enabled: Boolean) : LoginAction()
        class ShowLoginError(val resId: Int) : LoginAction()
        object HideLoginError : LoginAction()
        class Initialize(val state: State) : LoginAction()
    }

    @Parcelize
    data class State(
        val progressVisible: Boolean = false,
        val errorResId: Int? = null,
        val buttonResId: Int = R.string.sign_up,
        val toggleState: ToggleState = ToggleState.SIGNUP,
        val loginEnabled: Boolean = false,
        val errorShown: Boolean = false
    ) : Parcelable {
        enum class ToggleState {
            SIGNUP,
            LOGIN
        }
    }

    override fun updateState(action: LoginAction, state: State) =
        when (action) {
            is TransformToLogin -> state.copy(toggleState = ToggleState.LOGIN)
            is TransformToSignup -> state.copy(toggleState = ToggleState.SIGNUP)
            is SetProgressVisible -> state.copy(progressVisible = action.visible, buttonResId = action.buttonResId)
            is SetLoginButtonEnabled -> state.copy(loginEnabled = action.enabled)
            is ShowLoginError -> state.copy(errorResId = action.resId, errorShown = true)
            is HideLoginError -> state.copy(errorShown = false)
            else -> state
        }

    fun onCreate() {
        //Setup initial
        action(Initialize(state))
    }

    override fun getInitialState() = State()

    fun loginEmail(email: String, password: String) = viewModelScope.launch {
        if (state.toggleState == ToggleState.LOGIN) {
            action(SetProgressVisible(true, R.string.space))
            when (val result = LoginManager.login(email, password)) {
                is Success -> handleLoginSuccess()
                is Error -> handleError(result.error)
            }
            action(SetProgressVisible(false, getSignInButtonResId()))
        } else {
            val exists = LoginManager.checkUserExists(email)
            if (!exists) {
                action(LaunchPasswordActivity(email))
            } else {
                action(ShowLoginError(R.string.error_user_exists))
            }
        }
    }

    private fun getSignInButtonResId() =
        if (state.toggleState == ToggleState.SIGNUP) {
            R.string.sign_up
        } else {
            R.string.log_in
        }

    fun loginFacebook(supportFragmentManager: FragmentManager) = viewModelScope.launch {
        action(SetProgressVisible(true, R.string.space))
        try {
            when (val result = LoginManager.loginFacebook(supportFragmentManager)) {
                is Success -> handleLoginSuccess()
                is Error -> handleError(result.error)
            }
        } catch (e: CancellationException) {
            action(SetProgressVisible(false, getSignInButtonResId()))
        }
    }

    fun loginGoogle(activity: FragmentActivity) = viewModelScope.launch {
        action(SetProgressVisible(true, R.string.space))
        try {
            when (LoginManager.loginGoogle(activity)) {
                is Success -> handleLoginSuccess()
                is Error -> action(SetProgressVisible(false, getSignInButtonResId()))
            }
        } catch (e: CancellationException) {
            action(SetProgressVisible(false, getSignInButtonResId()))
        }
    }

    fun onFieldsUpdated(email: String, password: String) = viewModelScope.launch {
        val isValidEmail = LoginManager.isValidEmail(email)
        val emailError = !isValidEmail || email.isEmpty()
        val passwordError = password.isEmpty()
        val loginEnabled = if (state.toggleState == ToggleState.LOGIN) {
            !emailError && !passwordError
        } else {
            !emailError
        }
        action(SetLoginButtonEnabled(loginEnabled))
    }

    private suspend fun handleLoginSuccess() {
        try {
            val currentUser = userRepository.getCurrentUser()
            when {
                currentUser.username.isEmpty() -> action(LaunchUsernameActivity(isNewUser = false))
                Prefs.userLocation != null && locationHelper.hasAllPermissions -> {
                    Prefs.userLocation?.let { geofenceHelper.setGeofence(it) }
                    action(LaunchScoreActivity)
                }
                else -> action(LaunchWelcomeActivity)
            }
        } catch (e: ApolloException) {
            Timber.d("Unable to get current user $e")
            action(LaunchUsernameActivity(isNewUser = true))
        }
    }

    private fun handleError(error: Exception?) {
        action(SetProgressVisible(false, getSignInButtonResId()))
        Timber.d("Unable to log in: $error")
        action(ShowLoginError(R.string.login_error))
    }

    fun signUpToggleClicked() {
        action(HideLoginError)
        if (state.toggleState == ToggleState.SIGNUP) {
            action(TransformToLogin)
            ToggleState.LOGIN
        } else {
            action(TransformToSignup)
            ToggleState.SIGNUP
        }
    }
}
