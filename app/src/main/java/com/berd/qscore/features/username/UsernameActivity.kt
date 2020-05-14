package com.berd.qscore.features.username

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityUsernameBinding
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.username.UsernameViewModel.UsernameAction.*
import com.berd.qscore.features.welcome.WelcomeActivity
import com.berd.qscore.utils.extensions.createViewModel
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.setStatusbarColor
import com.berd.qscore.utils.extensions.visible
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import splitties.activities.start
import timber.log.Timber

class UsernameActivity : BaseActivity() {

    private val shouldLaunchWelcomeActivity by lazy {
        intent.getBooleanExtra(KEY_LAUNCH_WELCOME, false)
    }

    private val viewModel by lazy {
        createViewModel { handle ->
            UsernameViewModel(handle, shouldLaunchWelcomeActivity)
        }
    }

    private val binding: ActivityUsernameBinding by lazy {
        ActivityUsernameBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        observeEvents()
        setupViews()
    }

    private fun observeEvents() {
        viewModel.observeActions {
            when (it) {
                is Initialize -> initialize(it.state)
                is SetContinueEnabled -> setContinueEnabled(it.enabled)
                is ShowError -> showError()
                is SetProgressVisible -> setProgressVisible(it.visible)
                LaunchWelcomeActivity -> launchWelcomeActivity()
                FinishActivity -> finish()
            }
        }
    }

    private fun launchWelcomeActivity() {
        start<WelcomeActivity>()
        finish()
    }

    private fun initialize(state: UsernameViewModel.UsernameState) {
        if (state.hasError) {
            showError()
        }
        setProgressVisible(state.progressVisible)
        setContinueEnabled(state.continueEnabled)
    }

    private fun setProgressVisible(visible: Boolean) {
        if (visible) {
            binding.progress.visible()
            binding.continueButton.text = ""
            binding.usernameField.isEnabled = false
        } else {
            binding.progress.gone()
            binding.continueButton.text = getString(R.string.continue_button)
            binding.usernameField.isEnabled = true
        }
    }

    private fun showError() {
        binding.errorText.visible()
        binding.errorText.text = getString(R.string.unable_to_continue_please_try_again_later)
    }


    private fun setContinueEnabled(enabled: Boolean) {
        binding.continueButton.isEnabled = enabled
    }

    private fun setupViews() {
        setStatusbarColor(R.color.lighter_gray)
        setupPasswordField()
        binding.continueButton.setOnClickListener {
            val username = binding.usernameField.text.toString()
            viewModel.onContinue(username)
        }
    }

    private fun setupPasswordField() {
        binding.usernameField
            .afterTextChangeEvents()
            .skip(1)
            .map { it.editable.toString() }
            .distinctUntilChanged()
            .subscribeBy(onNext = {
                viewModel.onPasswordChange(it)
            }, onError = {
                Timber.d("Unable to handle textChange event: $it")
            }).addTo(compositeDisposable)
    }

    companion object {
        const val KEY_LAUNCH_WELCOME = "KEY_LAUNCH_WELCOME"

        fun newIntent(context: Context, shouldLaunchWelcomeActivity: Boolean) =
            Intent(context, UsernameActivity::class.java).apply {
                putExtra(KEY_LAUNCH_WELCOME, shouldLaunchWelcomeActivity)
            }
    }
}