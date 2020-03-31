package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivitySignupBinding
import com.berd.qscore.features.login.SignUpViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.login.SignUpViewModel.Action.LaunchWelcomeActivity
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
                is SignUpViewModel.State.FieldsUpdated -> handleFieldsUpdated(it.usernameError, it.emailError, it.passwordError, it.signUpIsReady)
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

    private fun handleFieldsUpdated(usernameError: Boolean, emailError: Boolean, passwordError: Boolean, signUpIsReady: Boolean) = binding.apply {
        if (usernameError) {
            usernameLayout.error = getString(R.string.username_error)
        } else if (!usernameLayout.error.isNullOrEmpty()) {
            usernameLayout.error = null
        }

        if (emailError) {
            emailLayout.error = getString(R.string.email_error)
        } else if (!emailLayout.error.isNullOrEmpty()){
            emailLayout.error = null
        }

        signup.isEnabled = signUpIsReady
    }

    private fun handleActions(it: SignUpViewModel.Action) {
        when (it) {
            LaunchScoreActivity -> launchScoreActivity()
            is LaunchWelcomeActivity -> launchWelcomeActivity()
        }
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
        val changeListener : () -> Unit = { viewModel.onFieldsUpdated(username.text.toString(),email.text.toString(),password.text.toString()) }
        username.onChange(changeListener)
        email.onChange(changeListener)
        password.onChange(changeListener)

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
}