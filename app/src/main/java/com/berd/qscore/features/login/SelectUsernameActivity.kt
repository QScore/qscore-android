package com.berd.qscore.features.login

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityUsernameBinding
import com.berd.qscore.features.login.SelectUsernameViewModel.Action.*
import com.berd.qscore.features.login.SelectUsernameViewModel.State.*
import com.berd.qscore.features.main.MainActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.onChangeDebounce
import com.berd.qscore.utils.extensions.showProgressDialog
import com.berd.qscore.utils.extensions.visible
import splitties.activities.start

class SelectUsernameActivity : BaseActivity() {
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
                LaunchScoreActivity -> launchScoreActivity()
                LaunchWelcomeActivity -> launchWelcomeActivity()
                is LaunchScoreActivity -> launchScoreActivity()
                ReturnToLogIn -> returnToLogIn()
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

    private fun returnToLogIn() {
        start<LoginActivity> {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        finish()
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
        usernameLayout.isEndIconVisible = true
        usernameLayout.helperText = getString(R.string.checking_usernames)
        if (!usernameLayout.error.isNullOrEmpty()) {
            usernameLayout.error = null
        }
    }

    private fun handleFieldsUpdated(usernameError: Boolean, signUpIsReady: Boolean) =
        binding.apply {
            usernameLayout.isEndIconVisible = false
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
        start<MainActivity>()
        progressDialog?.dismiss()
        finish()
    }

    private fun setupViews() = binding.apply {
        usernameInput.onChangeDebounce(500) {
            viewModel.onFieldsUpdated(usernameInput.text.toString())
        }
        startSpinnerAnimation()
        usernameLayout.isEndIconVisible = false

        continueButton.setOnClickListener {
            viewModel.onContinue(usernameInput.text.toString())
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun startSpinnerAnimation() = binding.apply {
        val spinnerAnimation = usernameLayout.endIconDrawable as Animatable
        AnimatedVectorDrawableCompat.registerAnimationCallback(
            usernameLayout.endIconDrawable,
            object : Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    usernameLayout.post { spinnerAnimation.start() }
                }
            })
        spinnerAnimation.start()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            viewModel.onBackPressed()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.onBackPressed()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
