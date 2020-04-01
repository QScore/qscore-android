package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import com.berd.qscore.utils.extensions.*
import com.facebook.CallbackManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
            ResetInProgress -> handleResetInProgress()
            LoginError -> handleLoginError()
            ResetError -> handleResetError()
            Ready -> handleReady()
            PasswordReset -> handlePasswordReset()
            is FieldsUpdated -> handleFieldsUpdated(
                state.emailError,
                state.passwordError,
                state.signUpIsReady
            )
        }
    }

    private fun handleReady() = binding.apply {
        progressDialog?.dismiss()
        errorText.invisible()
    }

    private fun handleLoginError() = binding.apply {
        progressDialog?.dismiss()
        errorText.text = getString(R.string.login_error)
        errorText.visible()
    }

    private fun handleResetError() = binding.apply {
        progressDialog?.dismiss()
        errorText.text = getString(R.string.reset_password_error)
        errorText.visible()
    }

    private fun handleInProgress() = binding.apply {
        progressDialog = showProgressDialog(getString(R.string.progress_message_login_password))
        errorText.invisible()
    }

    private fun handleResetInProgress() = binding.apply {
        progressDialog = showProgressDialog(getString(R.string.progress_message_reset_password))
        errorText.invisible()
    }

    private fun handlePasswordReset() = binding.apply {
        progressDialog?.dismiss()
        errorText.text = getString(R.string.reset_password_success)
        errorText.visible()
    }

    private fun handleFieldsUpdated(emailError: Boolean, passwordError: Boolean, signUpIsReady: Boolean) =
        binding.apply {
            if (emailError) {
                emailLayout.error = getString(R.string.email_error)
            } else if (!emailLayout.error.isNullOrEmpty()) {
                emailLayout.error = null
            }

            login.isEnabled = signUpIsReady
            gotoForgotText.isEnabled = !emailError
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
        val changeListener: () -> Unit =
            { viewModel.onFieldsUpdated(email.text.toString(), password.text.toString()) }
        email.onChange(changeListener)
        password.onChange(changeListener)

        login.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            viewModel.onLogin(email, password)
        }

        fbLogin.setOnClickListener {
            viewModel.loginFacebook(supportFragmentManager)
        }

        gotoSignUpText.setOnClickListener {
            launchSignUpActivity()
        }
        val spannable = SpannableString(getString(R.string.goto_sign_up))
        spannable.setSpan(ForegroundColorSpan(getColor(R.color.grey_400)), 0, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        gotoSignUpText.text = spannable

        gotoForgotText.setOnClickListener {
            MaterialAlertDialogBuilder(this@LoginActivity, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                .setTitle(getString(R.string.reset_password_title))
                .setMessage(getString(R.string.reset_password_message)+" "+email.text.toString())
                .setPositiveButton(getString(R.string.reset)){ dialog, which ->
                    // Do something for button click
                    // reset the password
                    viewModel.resetPassword(email.text.toString())
                }
                .setNegativeButton(getString(R.string.cancel),null)
                .show()
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