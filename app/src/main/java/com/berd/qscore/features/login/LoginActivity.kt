package com.berd.qscore.features.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import splitties.activities.start
import splitties.toast.toast
import timber.log.Timber


class LoginActivity : AppCompatActivity() {

    val callbackManager by lazy { CallbackManager.Factory.create() }

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        setupViews()
        setupFacebookLogin()
    }

    private fun setupViews() = binding.apply {
        signUp.setOnClickListener {
            start<SignupActivity> {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
        }
    }

    private fun setupFacebookLogin() = binding.apply {
        val emailPermission = "email"
        fbLogin.setPermissions(emailPermission)
        fbLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                Timber.d(">>Logged in with Facebook")
                toast("successful login: ${loginResult?.accessToken}")
            }

            override fun onCancel() {
                Timber.d(">>Facebook login cancelled")
            }

            override fun onError(exception: FacebookException) {
                Timber.e(">>Unable to login to facebook: $exception")
                toast(getString(R.string.unable_to_login_facebook))
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}