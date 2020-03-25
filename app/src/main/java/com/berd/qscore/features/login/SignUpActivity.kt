package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivitySignupBinding
import com.berd.qscore.features.login.confirmation.ConfirmActivity
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.features.login.LoginActivity
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
                SignUpViewModel.State.LoginError -> handleLoginError()
                SignUpViewModel.State.SignupError -> handleSignUpError()
                SignUpViewModel.State.Ready -> handleReady()
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

    private fun handleLoginError() {
        progressDialog?.dismiss()
        errorText.text = getString(R.string.login_error)
        errorText.visible()
    }

    private fun handleInProgress() {
        progressDialog = showProgressDialog("Signing in...")
        errorText.invisible()
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

    private fun setupViews() = binding.apply {
        login.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            viewModel.onLogin(email, password)
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