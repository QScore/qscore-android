package com.berd.qscore.features.password

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityPasswordBinding
import com.berd.qscore.features.password.PasswordViewModel.PasswordAction.*
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.username.UsernameActivity
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.setStatusbarColor
import com.berd.qscore.utils.extensions.visible
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class PasswordActivity : BaseActivity() {

    private val viewModel by viewModels<PasswordViewModel>()

    private val email by lazy {
        intent.getStringExtra(KEY_EMAIL)
    }

    private val binding: ActivityPasswordBinding by lazy {
        ActivityPasswordBinding.inflate(layoutInflater)
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
                is ShowError -> showError(it.message)
                LaunchUsernameActivity -> launchUsernameActivity()
                is SetProgressVisible -> setProgressVisible(it.visible)
            }
        }
    }

    private fun initialize(state: PasswordViewModel.PasswordState) {
        if (state.hasError) {
            showError(state.errorMessage)
        }
        setProgressVisible(state.progressVisible)
        setContinueEnabled(state.continueEnabled)
    }

    private fun setProgressVisible(visible: Boolean) {
        if (visible) {
            binding.progress.visible()
            binding.continueButton.text = ""
            binding.password.isEnabled = false
        } else {
            binding.progress.gone()
            binding.continueButton.text = getString(R.string.continue_button)
            binding.password.isEnabled = true
        }
    }

    private fun launchUsernameActivity() {
        val intent = UsernameActivity.newIntent(this, true)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String?) {
        binding.errorText.visible()
        val finalMessage = message ?: getString(R.string.unable_to_continue_please_try_again_later)
        binding.errorText.text = finalMessage
    }


    private fun setContinueEnabled(enabled: Boolean) {
        binding.continueButton.isEnabled = enabled
    }

    private fun setupViews() {
        setStatusbarColor(R.color.lighter_gray)
        setupPasswordField()
        binding.continueButton.setOnClickListener {
            val password = binding.password.text.toString()
            viewModel.onContinue(email, password)
        }
    }

    private fun setupPasswordField() {
        binding.password
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

        const val KEY_EMAIL = "KEY_EMAIL"

        fun newIntent(context: Context, email: String) =
            Intent(context, PasswordActivity::class.java).apply {
                putExtra(KEY_EMAIL, email)
            }
    }

}
