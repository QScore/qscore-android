package com.berd.qscore.features.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.berd.qscore.databinding.LeaderboardFragmentBinding
import com.berd.qscore.features.shared.activity.BaseFragment
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.visible
import timber.log.Timber

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
        observeEvents()
    }

    private fun observeEvents() {

    }

    private fun hideProgress() {
        binding.progressBar.invisible()
    }

    private fun showProgress() {
        Timber.d(">>SHOWING PROGRESS")
        binding.progressBar.visible()
    }

    private fun showUsers(users: List<QUser>) {
        leaderboardAdapter.submitList(users)
        binding.recyclerView.visible()
        binding.progressBar.invisible()
    }

    private fun handleEmptyResults() {
        binding.recyclerView.gone()
        binding.progressBar.invisible()
    }

    private fun setupRecyclerView() = activity?.let { activity ->
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = leaderboardAdapter
    }

    companion object {
        fun newInstance() = LeaderboardFragment()
    }
}
