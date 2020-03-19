package com.berd.qscore.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.login.LoginManager.SignupResult.NeedConfirmation
import com.berd.qscore.features.login.LoginManager.SignupResult.Success
import kotlinx.coroutines.launch
import timber.log.Timber

class SignupViewModel : ViewModel() {

    fun signup(email: String, password: String) = viewModelScope.launch {
        try {
            val result = LoginManager.signUp(email, password)
            when (result) {
                Success -> handleSuccess()
                is NeedConfirmation -> handleConfirmation()
            }
        } catch (e: Exception) {
            Timber.e(">>Unable to sign up: $e")
        }
    }

    private fun handleConfirmation() {
        Timber.d(">>Signup needs confirmation")
    }

    private fun handleSuccess() {
        Timber.d(">>Signup success")
    }

}