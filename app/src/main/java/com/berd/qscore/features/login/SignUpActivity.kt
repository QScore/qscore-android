package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivitySignupBinding
import com.berd.qscore.features.login.confirmation.ConfirmActivity
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.showProgressDialog
import com.berd.qscore.utils.extensions.visible
import com.facebook.CallbackManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_login.*
import splitties.activities.start
import timber.log.Timber

class SignUpActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val viewModel by viewModels<SignUpViewModel>()
    private var progressDialog: ProgressDialog? = null
    private var usernameReady: Boolean = false
    private var emailReady: Boolean = false
    private var passwordReady: Boolean = false

    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root

        if (savedInstanceState != null) {
            usernameReady = savedInstanceState.getBoolean(getString(R.string.key_username_ready),false)
            emailReady = savedInstanceState.getBoolean(getString(R.string.key_email_ready),false)
            passwordReady = savedInstanceState.getBoolean(getString(R.string.key_password_ready),false)
        }

        setContentView(view)
        observeEvents()
        setupViews()
    }

    private fun observeEvents() {
        viewModel.actions.subscribeBy(onNext = {
            handleActions(it)
        }, onError = {
            Timber.e("Error subscribing to events: $it")
        }).addTo(compositeDisposable)

        viewModel.state.observe(this, Observer {
            when (it) {
                SignUpViewModel.State.InProgress -> handleInProgress()
                SignUpViewModel.State.SignUpError -> handleSignUpError()
                SignUpViewModel.State.Ready -> handleReady()
                is SignUpViewModel.State.UsernameChange -> handleUsernameChange(it.usernameError, it.signUpIsReady)
                is SignUpViewModel.State.EmailChange -> handleEmailChange(it.emailError, it.signUpIsReady)
                is SignUpViewModel.State.PasswordChange -> handlePasswordChange(it.passwordError, it.signUpIsReady)
            }
        })
    }

    private fun handleSignUpError() = binding.apply {
        progressDialog?.dismiss()
        errorText.text = getString(R.string.sign_up_error)
        errorText.visible()
    }

    private fun handleReady() {
        progressDialog?.dismiss()
        errorText.gone()
    }

    private fun handleInProgress() {
        progressDialog = showProgressDialog("Signing in...")
        errorText.invisible()
    }

    private fun handleUsernameChange(usernameError: Boolean, signUpIsReady: Boolean) = binding.apply{
        if (usernameError) {
            usernameLayout.error = getString(R.string.username_error)
            usernameReady = false
        } else if (!usernameLayout.error.isNullOrEmpty()) {
            usernameLayout.error = null
            usernameReady = true
        } else {
            usernameReady = true
        }

        signup.isEnabled = signUpIsReady
    }

    private fun handleEmailChange(emailError: Boolean, signUpIsReady: Boolean) = binding.apply{
        if (emailError) {
            emailLayout.error = getString(R.string.email_error)
            emailReady = false
        } else if (!emailLayout.error.isNullOrEmpty()){
            emailLayout.error = null
            emailReady = true
        } else {
            emailReady = true
        }
        signup.isEnabled = signUpIsReady
    }

    private fun handlePasswordChange(passwordError: Boolean, signUpIsReady: Boolean) = binding.apply{
        if (passwordError) {
            passwordLayout.error = getString(R.string.password_error)
            passwordReady = false
        } else if (!passwordLayout.error.isNullOrEmpty()) {
            passwordLayout.error = null
            passwordReady = true
        } else {
            passwordReady = true
        }
        signup.isEnabled = signUpIsReady
    }

    private fun handleActions(it: SignUpViewModel.Action) {
        when (it) {
            SignUpViewModel.Action.LaunchScoreActivity -> launchScoreActivity()
            is SignUpViewModel.Action.LaunchConfirmActivity -> launchConfirmActivity(it.email)
            is SignUpViewModel.Action.LaunchWelcomeActivity -> launchWelcomeActivity()
        }
    }

    private fun launchWelcomeActivity() {
        start<WelcomeActivity>()
        progressDialog?.dismiss()
        finish()
    }

    private fun launchConfirmActivity(email: String) {
        val intent = ConfirmActivity.newIntent(this, email)
        progressDialog?.dismiss()
        startActivity(intent)
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

    private fun EditText.onChange(cb: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                cb(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupViews() = binding.apply {
        username.onChange { viewModel.checkUsername(username.text.toString(), emailReady, passwordReady)}
        email.onChange { viewModel.checkEmail(usernameReady ,email.text.toString(), passwordReady) }
        password.onChange { viewModel.checkPassword(usernameReady, emailReady, password.text.toString()) }

        signup.setOnClickListener {
            val username = username.text.toString()
            val email = email.text.toString()
            val password = password.text.toString()
            viewModel.onSignUp(username, email, password)
        }

        fbLogin.setOnClickListener {
            viewModel.loginFacebook(supportFragmentManager)
        }

        gotoLoginText.setOnClickListener {
            launchLoginActivity()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(getString(R.string.key_username_ready),usernameReady)
        outState.putBoolean(getString(R.string.key_email_ready),usernameReady)
        outState.putBoolean(getString(R.string.key_password_ready),usernameReady)
    }
}