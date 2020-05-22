package com.berd.qscore.resetpassword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityForgotPasswordBinding
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.username.UsernameActivity
import com.berd.qscore.resetpassword.ResetPasswordViewModel.*
import com.berd.qscore.resetpassword.ResetPasswordViewModel.ResetPasswordAction.*
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.setStatusbarColor
import com.berd.qscore.utils.extensions.visible
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import splitties.toast.longToast
import timber.log.Timber

class ResetPasswordActivity : BaseActivity() {

    private val viewModel by viewModels<ResetPasswordViewModel>()

    private val email by lazy {
        intent.getStringExtra(KEY_EMAIL)
    }

    private val binding: ActivityForgotPasswordBinding by lazy {
        ActivityForgotPasswordBinding.inflate(layoutInflater)
    }

    override fun getScreenName() = "SetPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        observeEvents()
        setupViews()
        viewModel.onCreate()
    }

    private fun observeEvents() {
        viewModel.observeActions {
            when (it) {
                is Initialize -> initialize(it.state)
                is SetResetEnabled -> setResetEnabled(it.enabled)
                is FinishActivity -> finishWithToast(it.email)
                is SetProgressVisible -> setProgressVisible(it.visible)
            }
        }
    }

    private fun initialize(state: ResetPasswordState) {
        setProgressVisible(state.showProgress)
        setResetEnabled(state.resetEnabled)
    }

    private fun setProgressVisible(visible: Boolean) {
        if (visible) {
            binding.progress.visible()
            binding.resetButton.text = ""
        } else {
            binding.progress.gone()
            binding.resetButton.text = getString(R.string.reset)
        }
    }

    private fun finishWithToast(email: String) {
        longToast(getString(R.string.reset_password_message, email))
        finish()
    }

    private fun launchUsernameActivity() {
        val intent = UsernameActivity.newIntent(this, true, false)
        startActivity(intent)
        finish()
    }


    private fun setResetEnabled(enabled: Boolean) {
        binding.resetButton.isEnabled = enabled
    }

    private fun setupViews() {
        setStatusbarColor(R.color.lighter_gray)
        setupEmailField()
        binding.resetButton.setOnClickListener {
            val email = binding.email.text.toString()
            viewModel.onReset(email)
        }
    }

    private fun setupEmailField() {
        binding.email
            .afterTextChangeEvents()
            .skip(1)
            .map { it.editable.toString() }
            .distinctUntilChanged()
            .subscribeBy(onNext = {
                viewModel.onEmailChange(it)
            }, onError = {
                Timber.d("Unable to handle textChange event: $it")
            }).addTo(compositeDisposable)
    }

    companion object {

        const val KEY_EMAIL = "KEY_EMAIL"

        fun newIntent(context: Context, email: String) =
            Intent(context, ResetPasswordActivity::class.java).apply {
                putExtra(KEY_EMAIL, email)
            }
    }

}
