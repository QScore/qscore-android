package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivitySignupBinding
import com.berd.qscore.features.login.SignUpViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.login.SignUpViewModel.Action.LaunchWelcomeActivity
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.showProgressDialog
import com.berd.qscore.utils.extensions.visible
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
                LaunchScoreActivity -> launchScoreActivity()
                is LaunchWelcomeActivity -> launchWelcomeActivity()
            }
        }
        viewModel.observeState {
            when (it) {
                SignUpViewModel.State.InProgress -> handleInProgress()
                SignUpViewModel.State.SignUpError -> handleSignUpError()
                SignUpViewModel.State.Ready -> handleReady()
                is SignUpViewModel.State.FieldsUpdated -> handleFieldsUpdated(
                    it.usernameError,
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
        progressDialog = showProgressDialog("Signing in...")
        errorText.invisible()
    }

    private fun handleFieldsUpdated(usernameError: Boolean, emailError: Boolean, passwordError: Boolean, signUpIsReady: Boolean) =
        binding.apply {
            if (usernameError) {
                usernameLayout.error = getString(R.string.username_error)
            } else if (!usernameLayout.error.isNullOrEmpty()) {
                usernameLayout.error = null
            }

            if (emailError) {
                emailsLayout.error = getString(R.string.email_error)
            } else if (!emailsLayout.error.isNullOrEmpty()) {
                emailsLayout.error = null
            }

            signup.isEnabled = signUpIsReady
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

    private fun EditText.onChange(cb: () -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                cb()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupViews() = binding.apply {
        val changeListener: () -> Unit =
            { viewModel.onFieldsUpdated(username.text.toString(), emails.text.toString(), password.text.toString()) }
        username.onChange(changeListener)
        emails.onChange(changeListener)
        password.onChange(changeListener)

        signup.setOnClickListener {
            val username = username.text.toString()
            val email = emails.text.toString()
            val password = password.text.toString()
            viewModel.onSignUp(username, email, password)
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