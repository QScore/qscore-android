package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivitySignupBinding
import com.berd.qscore.features.login.SignUpViewModel.Action.*
import com.berd.qscore.features.login.SignUpViewModel.State.*
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.utils.extensions.*
import com.facebook.CallbackManager
import splitties.activities.start

class SignUpActivity : BaseActivity() {

    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val viewModel by viewModels<SignUpViewModel>()
    private var progressDialog: ProgressDialog? = null

    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeEvents()
        setupViews()
    }

    private fun observeEvents() {
        viewModel.observeActions {
            when (it) {
                LaunchUsernameActivity -> launchUsernameActivity()
                LaunchScoreActivity -> launchScoreActivity()
                LaunchWelcomeActivity -> launchWelcomeActivity()
            }
        }
        viewModel.observeState {
            when (it) {
                InProgress -> handleInProgress()
                SignUpError -> handleSignUpError()
                Ready -> handleReady()
                is FieldsUpdated -> handleFieldsUpdated(
                    it.emailError,
                    it.passwordError,
                    it.signUpIsReady
                )
            }
        }
    }

    private fun handleSignUpError() = binding.apply {
        progressDialog?.dismiss()
        errorText.text = getString(R.string.sign_up_error)
        errorText.visible()
    }

    private fun handleReady() = binding.apply {
        progressDialog?.dismiss()
        errorText.gone()
    }

    private fun handleInProgress() = binding.apply {
        progressDialog = showProgressDialog(getString(R.string.progress_message_sign_up))
        errorText.invisible()
    }

    private fun handleFieldsUpdated(emailError: Boolean, passwordError: Boolean, signUpIsReady: Boolean) =
        binding.apply {
            if (emailError) {
                emailLayout.error = getString(R.string.email_error)
            } else if (!emailLayout.error.isNullOrEmpty()) {
                emailLayout.error = null
            }

            signup.isEnabled = signUpIsReady
        }

    private fun launchUsernameActivity() {
        start<SelectUsernameActivity>()
        progressDialog?.dismiss()
        finish()
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

    private fun launchLoginActivity() {
        start<LoginActivity>()
        progressDialog?.dismiss()
        finish()
    }

    private fun setupViews() = binding.apply {
        val changeListener: () -> Unit =
            { viewModel.onFieldsUpdated(email.text.toString(), password.text.toString()) }
        email.onChangeDebounce(500, changeListener)
        password.onChange(changeListener)

        signup.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            viewModel.onSignUp(email, password)
        }

        fbLogin.setOnClickListener {
            viewModel.loginFacebook(supportFragmentManager)
        }

        gotoLoginText.setOnClickListener {
            launchLoginActivity()
        }
        val spannable = SpannableString(getString(R.string.goto_login))
        spannable.setSpan(ForegroundColorSpan(Color.LTGRAY), 0, 24, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        gotoLoginText.text = spannable
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