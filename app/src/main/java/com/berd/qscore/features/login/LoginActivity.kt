package com.berd.qscore.features.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.databinding.ActivityLoginBinding
import com.berd.qscore.features.login.LoginViewModel.Action.*
import com.berd.qscore.features.login.confirmation.ConfirmActivity
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.facebook.CallbackManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import splitties.activities.start
import splitties.toast.toast
import timber.log.Timber


class LoginActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val viewModel by viewModels<LoginViewModel>()

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
        viewModel.actions.subscribeBy(onNext = {
            handleActions(it)
        }, onError = {
            Timber.e("Error subscribing to events: $it")
        }).addTo(compositeDisposable)
    }

    private fun handleActions(it: LoginViewModel.Action) {
        when (it) {
            LaunchScoreActivity -> launchScoreActivity()
            ShowError -> showError()
            is LaunchConfirmActivity -> launchConfirmActivity(it.email)
            is LaunchWelcomeActivity -> launchWelcomeActivity()
        }
    }

    private fun launchWelcomeActivity() {
        start<WelcomeActivity>()
        finish()
    }

    private fun launchConfirmActivity(email: String) {
        val intent = ConfirmActivity.newIntent(this, email)
        startActivity(intent)
        finish()
    }

    private fun launchScoreActivity() {
        start<ScoreActivity>()
        finish()
    }

    private fun showError() {
        toast("Unable to log in!")
    }

    private fun setupViews() = binding.apply {
        signUp.setOnClickListener {
            start<SignupActivity> {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
        }

        login.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            viewModel.onLogin(email, password)
        }

        fbLogin.setOnClickListener {
            viewModel.loginFacebook(supportFragmentManager)
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