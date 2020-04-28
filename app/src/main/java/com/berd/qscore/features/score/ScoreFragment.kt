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
    }

    companion object {
        fun newInstance() = ScoreFragment()
    }
}
