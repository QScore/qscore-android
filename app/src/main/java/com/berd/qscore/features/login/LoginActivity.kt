package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityLoginBinding
import com.berd.qscore.features.login.LoginViewModel.LoginAction
import com.berd.qscore.features.login.LoginViewModel.LoginAction.*
import com.berd.qscore.features.main.MainActivity
import com.berd.qscore.features.password.PasswordActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.username.UsernameActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.utils.extensions.*
import com.facebook.CallbackManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import splitties.activities.start
import splitties.dimensions.dp

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
        viewModel.onCreate()
    }

    private fun observeEvents() {
        viewModel.observeActions { handleActions(it) }
    }

    private fun handleActions(it: LoginAction) {
        when (it) {
            LaunchScoreActivity -> launchScoreActivity()
            is LaunchWelcomeActivity -> launchWelcomeActivity()
            LaunchUsernameActivity -> launchUsernameActivity()
            TransformToLogin -> transformToLogIn()
            TransformToSignup -> transformToSignup()
            LaunchResetPasswordActivity -> TODO()
            is LaunchPasswordActivity -> launchPasswordActivity(it.email)
            is SetProgressVisible -> setProgressVisible(it.visible)
            is SetLoginButtonEnabled -> setLoginButtonEnabled(it.enabled)
            is ShowLoginError -> showLoginError(it.resId)
            is Initialize -> setInitialState(it.state)
        }
    }

    private fun launchPasswordActivity(email: String) {
        val intent = PasswordActivity.newIntent(this, email)
        startActivity(intent)
    }

    private fun setInitialState(state: LoginViewModel.State) {
        setProgressVisible(state.progressVisible)
        state.errorResId?.let { showLoginError(it) }
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

    private fun setProgressVisible(visible: Boolean) {
        if (visible) {
            progressDialog = showProgressDialog(getString(R.string.progress_message_login))
        } else {
            progressDialog?.dismiss()
        }
    }

    private fun launchWelcomeActivity() {
        start<WelcomeActivity>()
        progressDialog?.dismiss()
        finish()
    }

    private fun launchUsernameActivity() {
        val intent = UsernameActivity.newIntent(this, true)
        startActivity(intent)
        progressDialog?.dismiss()
        finish()
    }

    private fun launchScoreActivity() {
        start<MainActivity>()
        progressDialog?.dismiss()
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
            showForgotPasswordDialog()
        }
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
    }

    private fun transformToLogIn() = binding.apply {
        passwordLayout.animateViewHeight(dp(60).toInt())
        forgotPassword.animateViewHeight(dp(60).toInt())
        loginWithEmail.text = getString(R.string.login_with_email)
        welcomeMessage.text = getString(R.string.welcome_back)
        loginButton.text = getString(R.string.log_in)
        signUpToggle.text = getString(R.string.sign_up)
        password.setText("")
        welcomeMessage.fadeIn()
        loginWithEmail.fadeIn()
    }

    private fun showForgotPasswordDialog() = binding.apply {
        MaterialAlertDialogBuilder(this@LoginActivity, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(getString(R.string.reset_password_title))
            .setMessage(getString(R.string.reset_password_message, email.text.toString()))
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                viewModel.resetPassword(email.text.toString())
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
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
