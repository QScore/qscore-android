package com.berd.qscore.features.login.confirmation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.berd.qscore.databinding.ActivityConfirmBinding
import com.berd.qscore.features.login.confirmation.ConfirmViewModel.Action
import com.berd.qscore.features.score.ScoreActivity
import com.berd.qscore.features.welcome.WelcomeActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import splitties.activities.start
import splitties.toast.toast
import timber.log.Timber

class ConfirmActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    private val binding by lazy {
        ActivityConfirmBinding.inflate(layoutInflater)
    }

    private val email by lazy {
        checkNotNull(intent.extras?.getString(EXTRA_EMAIL)) {
            "ConfirmActivity must have an email address!"
        }
    }

    private val viewModel by viewModels<ConfirmViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("ConfirmActivity has email: $email")
        setContentView(binding.root)
        setupListeners()
        observeActions()
    }

    private fun observeActions() {
        viewModel.actions.subscribeBy(onNext = {
            handleAction(it)
        }, onError = {
            Timber.e("Error subscribing to events: $it")
        }).addTo(compositeDisposable)
    }

    private fun handleAction(action: Action) {
        when (action) {
            Action.LaunchScoreActivity -> {
                start<ScoreActivity>() {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                finish()
            }
            Action.LaunchWelcomeActivity -> {
                start<WelcomeActivity>() {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                finish()
            }
            Action.ShowError -> toast("Invalid code, please try again")
            Action.ShowCodeToast -> toast("Code sent to email")
        }
    }

    private fun setupListeners() = binding.apply {
        confirmButton.setOnClickListener {
            viewModel.onConfirm(email, verifyNumber.text.toString())
        }

        resendButton.setOnClickListener {
            viewModel.onResend(email)
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_EMAIL = "EXTRA_EMAIL"
        fun newIntent(context: Context, email: String) =
            Intent(context, ConfirmActivity::class.java).apply {
                putExtra(EXTRA_EMAIL, email)
            }
    }


}