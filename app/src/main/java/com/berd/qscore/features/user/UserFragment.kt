package com.berd.qscore.features.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.berd.qscore.R
import com.berd.qscore.databinding.ScoreFragmentBinding
import com.berd.qscore.features.shared.activity.BaseFragment
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.user.UserViewModel.ScoreState.Loading
import com.berd.qscore.features.user.UserViewModel.ScoreState.Ready
import com.berd.qscore.utils.extensions.createViewModel
import com.berd.qscore.utils.extensions.loadAvatar
import com.berd.qscore.utils.extensions.visible
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import java.io.Serializable


class UserFragment : BaseFragment() {

    private val viewModel by lazy {
        createViewModel { UserViewModel(profileType) }
    }

    private val profileType by lazy {
        arguments?.getSerializable(KEY_PROFILE_TYPE) as ProfileType
    }

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
        user.avatar?.let { updateAvatar(it) }
        pullToRefresh.isRefreshing = false
        followersNumber.text = user.followerCount.toString()
        followingNumber.text = user.followingCount.toString()
        if (profileType != ProfileType.CurrentUser) {
            setupFollowButton(user)
        }
    }

    private fun setupFollowButton(user: QUser) = with(binding) {
        activity?.let { activity ->
            when {
                user.isCurrentUserFollowing -> {
                    followButton.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.colorPrimaryDark)
                    followButton.text = resources.getString(R.string.remove)
                    followButton.visible()
                }
                else -> {
                    followButton.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.light_gray)
                    followButton.text = resources.getString(R.string.add)
                    followButton.visible()
                }
            }
        }
    }

    private fun setupViews() = binding.apply {
        scoreProgress.progress = 1f
        avatarBorder.setOnClickListener { loadGiphy() }
        pullToRefresh.setOnRefreshListener { viewModel.onRefresh() }
    }

    private fun updateAvatar(url: String) {
        binding.avatarImage.loadAvatar(url)
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
                var image = media.images.fixedWidthSmall ?: return
                if (image.width > image.height) {
                    image = media.images.fixedHeight ?: return
                }
                val item = image.webPUrl ?: return
                viewModel.onGifAvatarSelected(item)
                updateAvatar(item)
            }
        }
    }

    companion object {
        val KEY_PROFILE_TYPE = "KEY_PROFILE_TYPE"

        fun newInstance(profileType: ProfileType) = UserFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_PROFILE_TYPE, profileType)
            }
        }
    }

    sealed class ProfileType : Serializable {
        object CurrentUser : ProfileType()
        class User(val userId: String) : ProfileType()
    }
}
