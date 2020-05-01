package com.berd.qscore.features.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.berd.qscore.databinding.LeaderboardFragmentBinding
import com.berd.qscore.features.leaderboard.LeaderboardViewModel.LeaderboardState.Ready
import com.berd.qscore.features.shared.activity.BaseFragment
import com.berd.qscore.features.shared.api.models.QLeaderboardScore
import com.berd.qscore.utils.extensions.gone

class LeaderboardFragment : BaseFragment() {

    private val viewModel by viewModels<LeaderboardViewModel>()

    private val leaderboardAdapter = LeaderboardAdapter()

    private val binding: LeaderboardFragmentBinding by lazy {
        LeaderboardFragmentBinding.inflate(layoutInflater)
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

    private fun handleReady(leaderboard: List<QLeaderboardScore>) {
        leaderboardAdapter.submitList(leaderboard)
        binding.pullToRefresh.isRefreshing = false
        binding.progressBar.gone()
    }

    private fun setupRecyclerView() = activity?.let { activity ->
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = leaderboardAdapter
    }

    companion object {
        fun newInstance() = LeaderboardFragment()
    }
}
