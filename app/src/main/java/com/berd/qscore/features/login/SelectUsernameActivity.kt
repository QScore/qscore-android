package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityUsernameBinding
import com.berd.qscore.features.login.SelectUsernameViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.login.SelectUsernameViewModel.Action.LaunchWelcomeActivity
import com.berd.qscore.features.login.SelectUsernameViewModel.State.*
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.onChange
import com.berd.qscore.utils.extensions.showProgressDialog
import com.berd.qscore.utils.extensions.visible
import com.facebook.CallbackManager
import splitties.activities.start

class SelectUsernameActivity : BaseActivity() {
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val viewModel by viewModels<SelectUsernameViewModel>()
    private var progressDialog: ProgressDialog? = null

    private val binding: ActivityUsernameBinding by lazy {
        ActivityUsernameBinding.inflate(layoutInflater)
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
                LaunchWelcomeActivity -> launchWelcomeActivity()
                is LaunchScoreActivity -> launchScoreActivity()
            }
        }
        viewModel.observeState {
            when (it) {
                InProgress -> handleInProgress()
                CheckingUsername -> handleCheckingUsername()
                ContinueError -> handleContinueError()
                Ready -> handleReady()
                is FieldsUpdated -> handleFieldsUpdated(
                    it.usernameError,
                    it.signUpIsReady
                )
            }
        }
    }

    private fun handleContinueError() = binding.apply {
        progressDialog?.dismiss()
        errorText.text = getString(R.string.continue_error)
        errorText.visible()
    }

    private fun handleReady() = binding.apply {
        progressDialog?.dismiss()
        errorText.invisible()
    }

    private fun handleInProgress() = binding.apply {
        progressDialog = showProgressDialog(getString(R.string.progress_message_username))
        errorText.invisible()
    }

    private fun handleCheckingUsername() = binding.apply {
        usernameLayout.error = null
        usernameLayout.helperText = getString(R.string.checking_usernames)
    }

    private fun handleFieldsUpdated(usernameError: Boolean, signUpIsReady: Boolean) =
        binding.apply {
            usernameLayout.helperText = getString(R.string.helper_username)

            if (usernameError) {
                usernameLayout.error = getString(R.string.username_error)
            } else if (!usernameLayout.error.isNullOrEmpty()) {
                usernameLayout.error = null
            }

            continueButton.isEnabled = signUpIsReady
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

    private fun setupViews() = binding.apply {
        val changeListener: () -> Unit =
            { viewModel.onFieldsUpdated(username.text.toString()) }
        username.onChange(changeListener)

        continueButton.setOnClickListener {
            viewModel.onContinue(username.text.toString())
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