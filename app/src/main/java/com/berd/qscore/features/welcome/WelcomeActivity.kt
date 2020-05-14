package com.berd.qscore.features.welcome

import android.app.ProgressDialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityWelcomeBinding
import com.berd.qscore.features.main.MainActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.*
import com.berd.qscore.utils.dialog.showDialogFragment
import com.berd.qscore.utils.extensions.setStatusbarColor
import com.berd.qscore.utils.extensions.showProgressDialog
import com.berd.qscore.utils.location.LocationHelper
import com.github.florent37.runtimepermission.kotlin.PermissionException
import kotlinx.coroutines.launch
import splitties.activities.start
import splitties.toast.toast
import timber.log.Timber

class WelcomeActivity : BaseActivity() {

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
        viewModel.observeActions {
            when (it) {
                LaunchScoreActivity -> launchScoreActivity()
                ShowLocationError -> showLocationError()
                is SetProgressDialogVisible -> setProgressVisible(it.visible)
            }
        }
    }

    override fun getScreenName() = "Welcome"

    private fun setProgressVisible(visible: Boolean) {
        if (visible) {
            progressDialog = showProgressDialog("Setting home location...")
        } else {
            progressDialog?.dismiss()
        }
    }

    private fun showLocationError() {
        toast("Unable to find location.  Please try again.")
    }

    private fun launchScoreActivity() {
        start<MainActivity>()
        finish()
    }

    private fun setupViews() = binding.let {
        setStatusbarColor(R.color.lighter_gray)
        it.userHomeButton.setOnClickListener {
            setupHomeLocation()
        }
    }

    private fun setupHomeLocation() {
        lifecycleScope.launch {
            try {
                if (LocationHelper.checkPermissions(this@WelcomeActivity)) {
                    viewModel.onContinue()
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
