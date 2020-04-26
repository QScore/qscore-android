package com.berd.qscore.features.welcome

import android.app.ProgressDialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.berd.qscore.databinding.ActivitySetupBinding
import com.berd.qscore.features.main.MainActivity
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.LaunchScoreActivity
import com.berd.qscore.features.welcome.WelcomeViewModel.Action.ShowError
import com.berd.qscore.features.welcome.WelcomeViewModel.State.FindingLocation
import com.berd.qscore.features.welcome.WelcomeViewModel.State.Ready
import com.berd.qscore.utils.extensions.showProgressDialog
import com.berd.qscore.utils.location.LocationHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.launch
import splitties.activities.start
import splitties.toast.toast
import timber.log.Timber

class WelcomeActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val binding: ActivitySetupBinding by lazy { ActivitySetupBinding.inflate(layoutInflater) }
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
            lifecycleScope.launch {
                if (LocationHelper.checkPermissions(this@WelcomeActivity)) {
                    viewModel.setupHome()
                } else {
                    toast("You must allow permissions to continue")
                }
            }
        }
    }
}
