package com.berd.qscore.features.welcome

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.R
import com.berd.qscore.databinding.ActivityWelcomeBinding
import com.berd.qscore.features.main.MainActivity
import com.berd.qscore.features.shared.activity.BaseActivity
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.*
import com.berd.qscore.utils.dialog.showDialogFragment
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.setStatusbarColor
import com.berd.qscore.utils.extensions.visible
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.location.LocationHelper
import com.github.florent37.runtimepermission.kotlin.PermissionException
import kotlinx.coroutines.launch
import splitties.activities.start
import splitties.toast.toast
import timber.log.Timber

class WelcomeActivity : BaseActivity() {

    private val binding: ActivityWelcomeBinding by lazy { ActivityWelcomeBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<WelcomeViewModel>()
    private val locationHelper by lazy {  Injector.locationHelper }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        observeActions()
        setupViews()
        viewModel.onCreate()
    }

    private fun observeActions() {
        viewModel.observeActions {
            when (it) {
                LaunchScoreActivity -> launchScoreActivity()
                ShowLocationError -> showLocationError()
                is SetProgressVisible -> setProgressVisible(it.visible)
                is Initialize -> initialize(it.state)
            }
        }
    }

    private fun initialize(state: WelcomeViewModel.State) {
        setProgressVisible(state.progressVisible)
    }

    override fun getScreenName() = "Welcome"

    private fun setProgressVisible(visible: Boolean) {
        if (visible) {
            binding.progress.visible()
            binding.userHomeButton.text = ""
            binding.userHomeButton.isEnabled = false
        } else {
            binding.userHomeButton.text = getString(R.string.i_m_home)
            binding.progress.gone()
            binding.userHomeButton.isEnabled = true
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
                if (locationHelper.checkPermissions(this@WelcomeActivity)) {
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

    private fun showNoPermissionConfirmationDialog() = showDialogFragment {
        this.title("Are you sure you want to continue?")
        this.message("QScore needs full location access to continue.  Your location will never leave the device.\n\nIf you continue without full location access, you will not be able to earn points.")
        this.positiveButtonResId(R.string.continue_dialog)
        this.negativeButtonResId(R.string.go_back)
        this.positiveButton { viewModel.continueWithoutLocation() }
        this.negativeButton { }
    }
}
