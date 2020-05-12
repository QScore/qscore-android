package com.berd.qscore.features.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.berd.qscore.databinding.LeaderboardFragmentBinding
import com.berd.qscore.features.leaderboard.LeaderboardViewModel.LeaderboardAction.SubmitPagedList
import com.berd.qscore.features.shared.activity.BaseFragmentWithState
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.user.UserActivity
import com.berd.qscore.utils.extensions.createViewModel
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.visible

class LeaderboardFragment : BaseFragmentWithState() {

    private val viewModel by lazy {
        createViewModel { handle -> LeaderboardViewModel(handle, leaderboardType) }
    }

    private val leaderboardType by lazy {
        arguments?.getSerializable(KEY_LEADERBOARD_TYPE) as LeaderboardType
    }

    private val leaderboardAdapter = LeaderboardAdapter(::handleLeaderboardClick)

    private val binding: LeaderboardFragmentBinding by lazy {
        LeaderboardFragmentBinding.inflate(layoutInflater)
    }

    private fun handleLeaderboardClick(userId: String) {
        activity?.let { activity ->
            val intent = UserActivity.newIntent(activity, userId)
            startActivity(intent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupViews()
        observeEvents()
        viewModel.onViewCreated()
    }

    private fun setupViews() {
        binding.pullToRefresh.setOnRefreshListener { viewModel.onRefresh() }
    }

    private fun observeEvents() {
        viewModel.observeActions {
            when (it) {
                is LeaderboardViewModel.LeaderboardAction.Initialize -> initialize(it.state)
                is SubmitPagedList -> handleSubmitPagedList(it.pagedList)
                is LeaderboardViewModel.LeaderboardAction.SetProgressShown -> setProgressShown(it.visible)
            }
        }
    }

    private fun initialize(state: LeaderboardViewModel.LeaderboardState) {
        setProgressShown(state.inProgress)
        state.pagedList?.let { handleSubmitPagedList(it) }
    }

    private fun setProgressShown(visible: Boolean) {
        if (visible) {
            binding.progressBar.visible()
        } else {
            binding.pullToRefresh.isRefreshing = false
            binding.progressBar.gone()
        }
    }

    private fun handleSubmitPagedList(pagedList: PagedList<QUser>) {
        leaderboardAdapter.submitList(pagedList)
    }

    private fun setupRecyclerView() = activity?.let { activity ->
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = leaderboardAdapter
    }

    companion object {
        const val KEY_LEADERBOARD_TYPE = "KEY_LEADERBOARD_TYPE"

        fun newInstance(leaderboardType: LeaderboardType) =
            LeaderboardFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_LEADERBOARD_TYPE, leaderboardType)
                }
            }
    }
}

