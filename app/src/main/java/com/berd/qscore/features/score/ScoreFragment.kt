package com.berd.qscore.features.score

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.berd.qscore.databinding.ScoreFragmentBinding
import com.berd.qscore.features.score.ScoreViewModel.ScoreState.Loading
import com.berd.qscore.features.score.ScoreViewModel.ScoreState.Ready
import com.berd.qscore.features.shared.activity.BaseFragment
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.utils.extensions.loadUrl
import com.bumptech.glide.request.RequestOptions
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import timber.log.Timber


class ScoreFragment : BaseFragment() {

    private val viewModel by viewModels<ScoreViewModel>()

    private val binding: ScoreFragmentBinding by lazy {
        ScoreFragmentBinding.inflate(layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Setup stuff
        observeEvents()
        setupViews()
        viewModel.onCreate()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun observeEvents() {
        viewModel.observeState { state ->
            when (state) {
                is Loading -> handleLoading()
                is Ready -> handleReady(state.user)
            }
        }
    }

    private fun handleLoading() = with(binding) {
        scoreProgress.progress = 0f
    }

    private fun handleReady(user: QUser) = with(binding) {
        username.text = user.username
        scoreProgress.progress = user.score.toFloat() / 100f
        allTimeScore.text = user.allTimeScore
        rankNumber.text = "#${user.rank}"
    }

    private fun setupViews() = binding.apply {
        scoreProgress.progress = 1f
        avatarBorder.setOnClickListener { loadGiphy() }
    }

    private fun updateAvatar(id: String) {
        binding.avatar.loadUrl("https://media.giphy.com/media/${id}/giphy.gif", customOptions = RequestOptions().circleCrop())
    }

    private fun loadGiphy() = activity?.let { activity ->
        Giphy.configure(activity.applicationContext, "LrZT9E87QWUQfNqmkWw6e86j64BSlCUR", verificationMode = true)
        val giphyDialog = GiphyDialogFragment.newInstance()
        giphyDialog.show(childFragmentManager, "giphy_dialog")
        giphyDialog.gifSelectionListener = object : GiphyDialogFragment.GifSelectionListener {
            override fun didSearchTerm(term: String) {
            }

            override fun onDismissed() {
            }

            override fun onGifSelected(media: Media) {
                //"https://giphy.com/embed/d2jibZKKA0k3RUgU"
                Timber.d(">>SELECTED: " + media)
                updateAvatar(media.id)
            }
        }
    }

    companion object {
        fun newInstance() = ScoreFragment()
    }
}
