package com.berd.qscore.features.welcome

import android.app.ProgressDialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityWelcomeBinding
import com.berd.qscore.features.main.MainActivity
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.ShowError
import com.berd.qscore.features.welcome.WelcomeViewModel.State.FindingLocation
import com.berd.qscore.features.welcome.WelcomeViewModel.State.Ready
import com.berd.qscore.utils.dialog.showDialogFragment
import com.berd.qscore.utils.extensions.showProgressDialog
import com.berd.qscore.utils.location.LocationHelper
import com.github.florent37.runtimepermission.kotlin.PermissionException
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.launch
import splitties.activities.start
import splitties.toast.toast
import timber.log.Timber

class WelcomeActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val binding: ActivityWelcomeBinding by lazy { ActivityWelcomeBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<WelcomeViewModel>()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeActions()
        setupViews()
    }

    private fun observeActions() {
        viewModel.actionsObservable.subscribeBy(onNext = {
            handleAction(it)
        }, onError = {
            Timber.e("Error subscribing to events: $it")
        }).addTo(compositeDisposable)

        viewModel.stateLiveData.observe(this, Observer {
            when (it) {
                FindingLocation -> {
                    progressDialog = showProgressDialog("Setting home location...")
                }
                Ready -> progressDialog?.dismiss()
            }
        })
    }

    private fun handleAction(action: WelcomeViewModel.Action) = when (action) {
        LaunchScoreActivity -> launchScoreActivity()
        ShowError -> showError()
    }

    private fun showError() {
        toast("Unable to find location.  Please try again.")
    }

    private fun launchScoreActivity() {
        start<MainActivity>()
        finish()
    }

    private fun setupViews() = binding.let {
        it.userHomeButton.setOnClickListener {
            setupHomeLocation()
        }
    }

    private fun setupHomeLocation() {
        lifecycleScope.launch {
            try {
                if (LocationHelper.checkPermissions(this@WelcomeActivity)) {
                    viewModel.onHomeClicked()
                } else {
                    showNoPermissionConfirmationDialog()
                }
            } catch (e: PermissionException) {
                Timber.d("Unable to grant permissions: $e")
                showNoPermissionConfirmationDialog()
            }
        }
    }

    private fun showNoPermissionConfirmationDialog() = binding.let {
        showDialogFragment {
            this.title("Are you sure you want to continue?")
            this.message("QScore needs full location access to continue.  Your location will never leave the device.\n\nIf you continue without full location access, you will not be able to earn points.")
            this.yesButtonResId(R.string.continue_dialog)
            this.noButtonResId(R.string.go_back)
            this.yesButton { viewModel.continueWithoutLocation() }
            this.noButton { }
        }
    }
}
