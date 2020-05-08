package com.berd.qscore.features.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.berd.qscore.databinding.LeaderboardFragmentBinding
import com.berd.qscore.features.leaderboard.LeaderboardViewModel.LeaderboardState.Ready
import com.berd.qscore.features.shared.activity.BaseFragment
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.user.UserActivity
import com.berd.qscore.utils.extensions.createViewModel
import com.berd.qscore.utils.extensions.gone

class LeaderboardFragment : BaseFragment() {

    private val viewModel by lazy {
        createViewModel { LeaderboardViewModel(leaderboardType) }
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
        viewModel.observeState {
            when (it) {
                is Ready -> handleReady(it.leaderboard)
            }
        }
    }

    private fun handleReady(leaderboard: List<QUser>) {
        leaderboardAdapter.submitList(leaderboard)
        binding.pullToRefresh.isRefreshing = false
        binding.progressBar.gone()
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

