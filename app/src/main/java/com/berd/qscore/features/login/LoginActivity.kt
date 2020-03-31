package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityLoginBinding
import com.berd.qscore.features.login.LoginViewModel.Action
import com.berd.qscore.features.login.LoginViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.login.LoginViewModel.Action.LaunchWelcomeActivity
import com.berd.qscore.features.login.LoginViewModel.State
import com.berd.qscore.features.login.LoginViewModel.State.*
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.showProgressDialog
import com.berd.qscore.utils.extensions.visible
import com.facebook.CallbackManager
import splitties.activities.start

class LoginActivity : BaseActivity() {

    private val viewModel by viewModels<LoginViewModel>()
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private var progressDialog: ProgressDialog? = null

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeEvents()
        setupViews()
    }

    private fun observeEvents() {
        viewModel.observeActions { handleActions(it) }
        viewModel.observeState { handleState(it) }
    }

    private fun handleActions(it: Action) {
        when (it) {
            LaunchScoreActivity -> launchScoreActivity()
            is LaunchWelcomeActivity -> launchWelcomeActivity()
        }
    }

    private fun handleState(state: State) {
        when (state) {
            InProgress -> handleInProgress()
            LoginError -> handleLoginError()
            Ready -> handleReady()
        }
    }

    private fun handleReady() = binding.apply {
        progressDialog?.dismiss()
        errorText.gone()
    }

    private fun handleLoginError() = binding.apply {
        progressDialog?.dismiss()
        errorText.text = getString(R.string.login_error)
        errorText.visible()
    }

    private fun handleInProgress() = binding.apply {
        progressDialog = showProgressDialog("Signing in...")
        errorText.invisible()
    }

    private fun launchWelcomeActivity() {
        start<WelcomeActivity>()
        progressDialog?.dismiss()
        finish()
    }

    private fun launchScoreActivity() {
        start<ScoreActivity>()
        progressDialog?.dismiss()
        finish()
    }

    private fun launchSignUpActivity() {
        start<SignUpActivity>()
        progressDialog?.dismiss()
        finish()
    }

    private fun setupViews() = binding.apply {
        login.setOnClickListener {
            val username = username.text.toString()
            val password = password.text.toString()
            viewModel.onLogin(username, password)
        }

        fbLogin.setOnClickListener {
            viewModel.loginFacebook(supportFragmentManager)
        }

        gotoSignUpText.setOnClickListener {
            launchSignUpActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

}