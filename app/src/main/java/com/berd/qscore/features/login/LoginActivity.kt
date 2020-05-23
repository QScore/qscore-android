package com.berd.qscore.features.login

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityLoginBinding
import com.berd.qscore.features.login.LoginViewModel.LoginAction
import com.berd.qscore.features.login.LoginViewModel.LoginAction.*
import com.berd.qscore.features.main.MainActivity
import com.berd.qscore.features.setpassword.PasswordActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.username.UsernameActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.resetpassword.ResetPasswordActivity
import com.berd.qscore.utils.extensions.*
import com.facebook.CallbackManager
import splitties.activities.start
import splitties.dimensions.dp

class LoginActivity : BaseActivity() {

    private val viewModel by viewModels<LoginViewModel>()
    private val callbackManager by lazy { CallbackManager.Factory.create() }

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeEvents()
        setupViews()
        viewModel.onCreate()
    }

    private fun observeEvents() {
        viewModel.observeActions { handleActions(it) }
    }

    private fun handleActions(it: LoginAction) {
        when (it) {
            LaunchScoreActivity -> launchScoreActivity()
            is LaunchWelcomeActivity -> launchWelcomeActivity()
            is LaunchUsernameActivity -> launchUsernameActivity(it.isNewUser)
            TransformToLogin -> transformToLogIn()
            TransformToSignup -> transformToSignup()
            is LaunchPasswordActivity -> launchPasswordActivity(it.email)
            is SetProgressVisible -> setProgressVisible(it.visible, it.buttonResId)
            is SetLoginButtonEnabled -> setLoginButtonEnabled(it.enabled)
            is ShowLoginError -> showLoginError(it.resId)
            is Initialize -> setInitialState(it.state)
            HideLoginError -> hideLoginError()
        }
    }

    private fun hideLoginError() {
        binding.errorText.invisible()
    }

    private fun launchPasswordActivity(email: String) {
        val intent = PasswordActivity.newIntent(this, email)
        startActivity(intent)
    }

    private fun setInitialState(state: LoginViewModel.State) {
        setProgressVisible(state.progressVisible, state.buttonResId)
        if (state.errorShown) {
            if (state.errorResId != null) {
                showLoginError(state.errorResId)
            }
        } else {
            hideLoginError()
        }
        if (state.toggleState == LoginViewModel.State.ToggleState.LOGIN) {
            transformToLogIn()
        } else {
            transformToSignup()
        }
        setLoginButtonEnabled(state.loginEnabled)
    }

    private fun showLoginError(resId: Int) = binding.apply {
        errorText.text = getString(resId)
        errorText.visible()
    }

    private fun setLoginButtonEnabled(enabled: Boolean) {
        binding.loginButton.isEnabled = enabled
    }

    private fun setProgressVisible(visible: Boolean, buttonResId: Int) {
        if (visible) {
            binding.progress.visible()
            binding.loginButton.text = ""
            binding.loginButton.isEnabled = false
        } else {
            binding.progress.gone()
            binding.loginButton.text = getString(buttonResId)
            binding.loginButton.isEnabled = true
        }
    }

    private fun launchWelcomeActivity() {
        start<WelcomeActivity>()
        finish()
    }

    private fun launchUsernameActivity(newUser: Boolean) {
        val intent = UsernameActivity.newIntent(this, newUser, shouldLaunchWelcomeActivity = true)
        startActivity(intent)
        finish()
    }

    private fun launchScoreActivity() {
        start<MainActivity>()
        finish()
    }

    private fun setupViews() = binding.apply {
        setStatusbarColor(R.color.lighter_gray)
        val changeListener: () -> Unit = {
            viewModel.onFieldsUpdated(email.text.toString(), password.text.toString())
        }
        email.onChange(changeListener)
        password.onChange(changeListener)

        email.setOnClickListener {
            window.setSoftInputMode(SOFT_INPUT_STATE_HIDDEN)
        }


        signUpToggle.setOnClickListener {
            viewModel.signUpToggleClicked()
        }

        loginButton.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            viewModel.loginEmail(email, password)
        }

        fbLogin.setOnClickListener {
            viewModel.loginFacebook(supportFragmentManager)
        }

        googleLogin.setOnClickListener {
            viewModel.loginGoogle(this@LoginActivity)
        }

        forgotPassword.setOnClickListener {
            launchForgotPasswordActivity()
        }
    }

    private fun launchForgotPasswordActivity() {
        start<ResetPasswordActivity>()
    }

    private fun transformToSignup() = binding.apply {
        passwordLayout.animateViewHeight(0)
        forgotPassword.animateViewHeight(0)
        loginWithEmail.text = getString(R.string.sign_up_with_email)
        loginButton.text = getString(R.string.sign_up)
        welcomeMessage.text = getString(R.string.create_an_account)
        signUpToggle.text = getString(R.string.log_in)
        welcomeMessage.fadeIn()
        loginWithEmail.fadeIn()
        password.setText("")
    }

    private fun transformToLogIn() = binding.apply {
        passwordLayout.animateViewHeight(dp(60).toInt())
        forgotPassword.animateViewHeight(dp(60).toInt())
        loginWithEmail.text = getString(R.string.login_with_email)
        welcomeMessage.text = getString(R.string.welcome_back)
        loginButton.text = getString(R.string.log_in)
        signUpToggle.text = getString(R.string.sign_up)
        welcomeMessage.fadeIn()
        loginWithEmail.fadeIn()
        setLoginButtonEnabled(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun getScreenName() = "Login"

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

}
