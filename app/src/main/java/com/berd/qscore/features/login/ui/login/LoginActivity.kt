package com.berd.qscore.features.login.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import splitties.toast.toast
import timber.log.Timber


class LoginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        setupFacebookLogin()
    }

    private fun setupFacebookLogin() = binding.apply {
        val callbackManager = CallbackManager.Factory.create();
        val emailPermission = "email"
        fbLogin.setPermissions(emailPermission)
        fbLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                toast("successful login: ${loginResult?.accessToken}")
            }

            override fun onCancel() {
                //Do nothing
            }

            override fun onError(exception: FacebookException) {
                Timber.e("Unable to login to facebook: $exception")
                toast(getString(R.string.unable_to_login_facebook))
            }
        })
    }

}