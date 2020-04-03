package com.berd.qscore.features.login

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.berd.qscore.features.login.LoginManager.AuthEvent.Error
import com.berd.qscore.features.login.LoginManager.AuthEvent.Success
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.utils.injection.Injector
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.IOException
import java.util.regex.Pattern
import kotlin.coroutines.resume


object LoginManager {
    private val fbCallbackManager: CallbackManager by lazy { CallbackManager.Factory.create() }
    private val fbLoginManager = Injector.fbLoginmanager
    private val firebaseAuth = Injector.firebaseAuth

    val isLoggedIn get() = firebaseAuth.currentUser != null
    val emailPattern: Pattern by lazy {
        val expression =
            "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
        Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    }

    sealed class AuthEvent {
        object Success : AuthEvent()
        class Error(val error: Exception?) : AuthEvent()
    }

    private val Task<*>.resultEvent get() = if (isSuccessful) Success else Error(exception)

    suspend fun signUp(email: String, password: String) = suspendCancellableCoroutine<AuthEvent> {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            it.resume(task.resultEvent)
        }
    }

    suspend fun login(email: String, password: String) = suspendCancellableCoroutine<AuthEvent> {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            it.resume(task.resultEvent)
        }
    }

    suspend fun sendPasswordResetEmail(email: String) = suspendCancellableCoroutine<AuthEvent> {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            it.resume(task.resultEvent)
        }
    }

    suspend fun checkUserHasUsername(): Boolean {
        if (!isLoggedIn) {
            return false
        }

        return try {
            val currentUser = Api.getCurrentUser()
            if (currentUser.username.isNullOrEmpty()) {
                return false
            }
            true
        } catch (e: IOException) {
            //Current user does not exist
            false
        }
    }

    suspend fun loginFacebook(supportFragmentManager: FragmentManager) = suspendCancellableCoroutine<AuthEvent> { cont ->
        val fbTokenCallback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val token = loginResult.accessToken.token
                Timber.d("Successful login with facebook, continuing to aws")
                val credential = FacebookAuthProvider.getCredential(token)
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener {
                        Timber.d(">>TOKEN: ${it.result?.token}")
                    }
                    cont.resume(task.resultEvent)
                }
            }

            override fun onCancel() {
                cont.cancel()
            }

            override fun onError(error: FacebookException) {
                cont.resume(Error(error))
            }
        }

        val callbackFragment = CallbackFragment.create(supportFragmentManager)
        fbLoginManager.registerCallback(fbCallbackManager, fbTokenCallback)
        fbLoginManager.logInWithReadPermissions(callbackFragment, mutableListOf("email"))
    }


    fun logout() {
        firebaseAuth.signOut()
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
}