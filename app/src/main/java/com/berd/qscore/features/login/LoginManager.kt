package com.berd.qscore.features.login

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.*
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobile.client.results.SignUpResult
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails
import com.berd.qscore.features.login.LoginManager.SignupEvent.NeedConfirmation
import com.berd.qscore.features.login.LoginManager.SignupEvent.Success
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object LoginManager {
    private val client by lazy { AWSMobileClient.getInstance() }
    private val fbCallbackManager: CallbackManager by lazy { CallbackManager.Factory.create() }
    private val fbLoginManager by lazy { LoginManager.getInstance() }

    val isLoggedIn get() = client.isSignedIn

    sealed class SignupEvent {
        object Success : SignupEvent()
        class NeedConfirmation(val details: UserCodeDeliveryDetails) : SignupEvent()
    }

    sealed class LoginEvent {
        object Success : LoginEvent()
        object Unknown : LoginEvent()
    }

    suspend fun signUp(email: String, password: String) = suspendCancellableCoroutine<SignupEvent> {
        val username = email
        val attributes: MutableMap<String, String> = HashMap()
        attributes["email"] = email
        client.signUp(username, password, attributes, null, object : Callback<SignUpResult> {
            override fun onResult(result: SignUpResult) = runOnUiThread {
                if (!result.confirmationState) {
                    it.resume(NeedConfirmation(result.userCodeDeliveryDetails))
                } else {
                    it.resume(Success)
                }
            }

            override fun onError(e: Exception) = runOnUiThread {
                Timber.d("Unable to sign up: $e")
                it.resumeWithException(e)
            }
        })
    }

    suspend fun completeSignUp(email: String, code: String) = suspendCancellableCoroutine<SignupEvent> {
        client.confirmSignUp(email, code, object : Callback<SignUpResult> {
            override fun onResult(result: SignUpResult) = runOnUiThread {
                if (!result.confirmationState) {
                    it.resume(NeedConfirmation(result.userCodeDeliveryDetails))
                } else {
                    it.resume(Success)
                }
            }

            override fun onError(e: Exception) = runOnUiThread {
                Timber.d("Unable to sign up: $e")
                it.resumeWithException(e)
            }
        })
    }

    suspend fun login(email: String, password: String) = suspendCancellableCoroutine<LoginEvent> {
        client.signIn(email, password, null, object : Callback<SignInResult> {
            override fun onResult(signInResult: SignInResult) = runOnUiThread {
                Timber.d("Sign-in callback state: ${signInResult.signInState}")
                val result = when (signInResult.signInState) {
                    SignInState.DONE -> LoginEvent.Success
                    else -> LoginEvent.Unknown
                }
                it.resume(result)
            }

            override fun onError(e: Exception) {
                Timber.d("Error logging in: $e")
                it.resumeWithException(e)
            }
        })
    }

    suspend fun sendConfirmationCode(username: String) = suspendCancellableCoroutine<Unit> {
        AWSMobileClient.getInstance().resendSignUp(username, object : Callback<SignUpResult> {
            override fun onResult(signUpResult: SignUpResult) {
                Timber.d("Send confirmation $signUpResult")
                it.resume(Unit)
            }

            override fun onError(e: Exception) {
                Timber.e("Unable to resend confirmation code")
                it.resumeWithException(e)
            }
        })
    }

    suspend fun loginFacebook(supportFragmentManager: FragmentManager) = suspendCancellableCoroutine<LoginEvent> { cont ->
        val fbTokenCallback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val token = loginResult.accessToken.token
                Timber.d("Successful login with facebook, continuing to aws")
                client.federatedSignIn(IdentityProvider.FACEBOOK.toString(), token, object : Callback<UserStateDetails> {
                    override fun onResult(userStateDetails: UserStateDetails) {
                        val result = when (userStateDetails.userState) {
                            UserState.SIGNED_IN -> LoginEvent.Success
                            else -> LoginEvent.Unknown
                        }
                        cont.resume(result)
                    }

                    override fun onError(e: Exception) {
                        cont.resumeWithException(e)
                        Timber.d("Unable to sign into aws via Facebook: $e")
                    }
                })
            }

            override fun onCancel() {
                Timber.d("Facebook login cancelled")
                cont.cancel()
            }

            override fun onError(error: FacebookException) {
                cont.resumeWithException(error)
            }
        }

        val callbackFragment = CallbackFragment.create(supportFragmentManager)
        fbLoginManager.registerCallback(fbCallbackManager, fbTokenCallback)
        fbLoginManager.logInWithReadPermissions(callbackFragment, mutableListOf("email"))
    }


    @Suppress("UNCHECKED_CAST")
    class CallbackFragment : Fragment() {
        init {
            retainInstance = true
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            fbCallbackManager.onActivityResult(requestCode, resultCode, data)
            super.onActivityResult(requestCode, resultCode, data)
            removeFragment()
        }


        private fun removeFragment() = activity?.supportFragmentManager?.let {
            it.beginTransaction().remove(this).commitNowAllowingStateLoss()
            it.executePendingTransactions()
        }

        companion object {
            const val FRAGMENT_TAG = "FACEBOOK_FRAGMENT_TAG"
            fun create(supportFragmentManager: FragmentManager): CallbackFragment {
                return supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as? CallbackFragment
                    ?: CallbackFragment().also {
                        supportFragmentManager.beginTransaction().add(it, FRAGMENT_TAG).commitNowAllowingStateLoss()
                        supportFragmentManager.executePendingTransactions()
                    }
            }
        }
    }

    suspend fun confirmCode(code: String) = suspendCancellableCoroutine<LoginEvent> {
        client.confirmSignIn(code, object : Callback<SignInResult> {
            override fun onResult(signInResult: SignInResult) {
                Timber.d("Sign-in callback state: ${signInResult.signInState}")
                val result = when (signInResult.signInState) {
                    SignInState.DONE -> LoginEvent.Success
                    else -> LoginEvent.Unknown
                }
                it.resume(result)
            }

            override fun onError(e: java.lang.Exception) {
                Timber.d("Error confirming: $e")
                it.resumeWithException(e)
            }
        })
    }
}