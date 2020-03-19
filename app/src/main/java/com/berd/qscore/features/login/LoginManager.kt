package com.berd.qscore.features.login

import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignUpResult
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails
import com.berd.qscore.features.login.LoginManager.SignupResult.NeedConfirmation
import com.berd.qscore.features.login.LoginManager.SignupResult.Success
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object LoginManager {
    val isLoggedIn = AWSMobileClient.getInstance().isSignedIn

    fun checkLoggedIn() {
        AWSMobileClient.getInstance().isSignedIn
    }

    sealed class SignupResult {
        object Success : SignupResult()
        class NeedConfirmation(val details: UserCodeDeliveryDetails) : SignupResult()
    }

    suspend fun signUp(email: String, password: String) =
        suspendCancellableCoroutine<SignupResult> {
            val username = email
            val attributes: MutableMap<String, String> = HashMap()
            attributes["email"] = "name@email.com"
            AWSMobileClient.getInstance()
                .signUp(username, password, attributes, null, object : Callback<SignUpResult> {
                    override fun onResult(result: SignUpResult) {
                        if (!result.confirmationState) {
                            it.resume(NeedConfirmation(result.userCodeDeliveryDetails))
                        } else {
                            it.resume(Success)
                        }
                    }

                    override fun onError(e: Exception) {
                        Timber.d("Unable to sign up: $e")
                        it.resumeWithException(e)
                    }
                })
        }


}