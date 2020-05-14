package com.berd.qscore.features.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.berd.qscore.R
import com.berd.qscore.databinding.UserFragmentBinding
import com.berd.qscore.features.geofence.GeofenceStatus
import com.berd.qscore.features.main.MainActivity
import com.berd.qscore.features.shared.activity.BaseFragment
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.secrets.SecretManager
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.user.UserListActivity.UserListType.FOLLOWED
import com.berd.qscore.features.user.UserListActivity.UserListType.FOLLOWERS
import com.berd.qscore.features.user.UserViewModel.UserAction.*
import com.berd.qscore.features.user.UserViewModel.UserState.Loading
import com.berd.qscore.features.user.UserViewModel.UserState.Ready
import com.berd.qscore.utils.extensions.*
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

    private val binding: UserFragmentBinding by lazy {
        UserFragmentBinding.inflate(layoutInflater)
    }

    private val isCurrentUser
        get() = profileType.let {
            when (it) {
                is ProfileType.CurrentUser -> true
                is ProfileType.User -> it.userId == UserRepository.currentUser?.userId
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Setup stuff
        setupViews()
        setupToolbar()
        observeEvents()
        viewModel.onCreate()
    }

    private fun setupToolbar() {
        val activity = this.activity as? AppCompatActivity
        activity?.apply {
            setSupportActionBar(binding.toolbar)
            if (this !is MainActivity) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = ""
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            activity?.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun getScreenName() = if (isCurrentUser) "CurrentUserProfile" else "UserProfile"

    private fun observeEvents() {
        viewModel.observeActions { action ->
            when (action) {
                is LaunchFollowingUserList -> launchFollowingUserList(action.userId)
                is LaunchFollowersUserList -> launchFollowersUserList(action.userId)
                is SetGeofenceStatus -> handleGeofenceStatus(action.status)
            }
        }

        viewModel.observeState { state ->
            when (state) {
                is Loading -> handleLoading()
                is Ready -> handleReady(state.user)
            }
        }
    }

    private fun handleGeofenceStatus(status: GeofenceStatus) {
        val colorResId = when (status) {
            GeofenceStatus.HOME -> R.color.colorPrimary
            GeofenceStatus.AWAY -> R.color.punch_red
        }
        binding.toolbar.setBackgroundResource(colorResId)
        setStatusbarColor(colorResId)
        binding.topBg.setBackgroundColorResId(colorResId)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        viewModel.onHiddenChanged(hidden)
    }

    private fun launchFollowingUserList(userId: String) = activity?.let { activity ->
        val intent = UserListActivity.newIntent(activity, userId, FOLLOWED)
        startActivity(intent)
    }

    private fun launchFollowersUserList(userId: String) = activity?.let { activity ->
        val intent = UserListActivity.newIntent(activity, userId, FOLLOWERS)
        startActivity(intent)
    }

    private fun handleLoading() = with(binding) {
        scoreProgress.progress = 0f
        progress.visible()
    }

    private fun handleReady(user: QUser) = with(binding) {
        username.text = user.username
        scoreProgress.progress = user.score.toFloat() / 100
        allTimeScore.text = user.allTimeScore
        rankNumber.text =
            if (user.rank.isNullOrEmpty() || user.rank == "0" || user.rank == "Unknown") {
                "Unknown"
            } else {
                "#${user.rank}"
            }
        setupAvatar(user)
        pullToRefresh.isRefreshing = false
        pullToRefresh.setColorSchemeColors(getColor(R.color.colorAccent))
        followersNumber.text = user.followerCount.toString()
        followingNumber.text = user.followingCount.toString()
        progress.gone()
        if (isCurrentUser) {
            scoreProgress.visible()
        } else {
            setupFollowButton(user)
            bottomScoreLabel.gone()
        }
    }

    private fun setupAvatar(user: QUser) {
        user.avatar?.let { updateAvatar(it) }
            ?: binding.avatarImage.loadDefaultAvatar(user.userId)
    }

    private fun setupFollowButton(user: QUser) = with(binding) {
        activity?.let { activity ->
            when {
                user.isCurrentUserFollowing -> {
                    followButton.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.light_gray)
                    followButton.text = resources.getString(R.string.remove)
                    followButton.visible()
                    followButton.setOnClickListener { viewModel.onFollowButtonClicked(user.userId, UserViewModel.FollowType.UNFOLLOW) }
                }
                else -> {
                    followButton.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.colorPrimaryDark)
                    followButton.text = resources.getString(R.string.add)
                    followButton.visible()
                    followButton.setOnClickListener { viewModel.onFollowButtonClicked(user.userId, UserViewModel.FollowType.FOLLOW) }
                }
            }
        }
    }

    private fun setupViews() = binding.apply {
        if (isCurrentUser) {
            scoreProgress.progress = 1f
            avatarBorder.setOnClickListener {
                viewModel.onAvatarClicked()
                loadGiphy()
            }
            pullToRefresh.setOnRefreshListener { viewModel.onRefresh() }
        } else {
            pullToRefresh.isEnabled = false
        }

        followersNumber.setOnClickListener {
            viewModel.onFollowersClicked()
        }

        followingNumber.setOnClickListener {
            viewModel.onFollowingClicked()
        }
    }

    private fun updateAvatar(url: String) {
        binding.avatarImage.loadAvatar(url)
    }

    private fun loadGiphy() = activity?.let { activity ->
        Giphy.configure(activity.applicationContext, SecretManager.giphyKey, verificationMode = false)
        val giphyDialog = GiphyDialogFragment.newInstance()
        giphyDialog.show(childFragmentManager, "giphy_dialog")
        giphyDialog.onGifSelected { media -> setGifAsAvatar(media) }
    }

    private fun setGifAsAvatar(media: Media) {
        var image = media.images.fixedWidthSmall ?: return
        if (image.width > image.height) {
            image = media.images.fixedHeightSmall ?: return
        }
        val item = image.webPUrl ?: return
        viewModel.onGifAvatarSelected(item)
        updateAvatar(item)
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
