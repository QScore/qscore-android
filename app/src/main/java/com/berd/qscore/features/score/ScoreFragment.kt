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
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
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
        setupViews()
        observeEvents()
        viewModel.onCreate()
        Timber.d(">>ON VIEW CREATED")
    }

    override fun onDestroy() {
        Timber.d(">>VIEW DESTROYED")
        super.onDestroy()
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
        scoreProgress.progress = user.score.toFloat() / 100
        allTimeScore.text = user.allTimeScore
        rankNumber.text = "#${user.rank}"
    }

    private fun setupViews() = binding.apply {
        scoreProgress.progress = 1f
        avatarBorder.setOnClickListener { loadGiphy() }
    }

    private fun updateAvatar(url: String) {

        Glide.with(this) //.asBitmap()
            .load(url)
            .optionalTransform(CircleCrop())
            .optionalTransform(WebpDrawable::class.java, WebpDrawableTransformation(CircleCrop()))
            .into(binding.avatar)

//        binding.avatar.loadUrl(url, customOptions = RequestOptions().circleCrop())
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
                val item = media.images.fixedWidthSmall
                updateAvatar(item?.webPUrl ?: "")
                Timber.d(">>SIZE: " + item?.webPSize)
            }
        }
    }

    companion object {
        fun newInstance() = ScoreFragment()
    }
}
