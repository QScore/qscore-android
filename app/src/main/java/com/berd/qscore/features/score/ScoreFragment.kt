package com.berd.qscore.features.score

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.berd.qscore.R
import com.berd.qscore.databinding.ScoreFragmentBinding
import com.berd.qscore.features.score.ScoreViewModel.ScoreState.Loading
import com.berd.qscore.features.score.ScoreViewModel.ScoreState.Ready
import com.berd.qscore.features.shared.activity.BaseFragment


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
                is Ready -> handleReady(state.score, state.allTimeScore)
            }
        }
    }

    private fun handleLoading() = with(binding) {
        scoreProgress.progress = 0f
    }

    private fun handleReady(score: Int, allTimeScore: Int) = with(binding) {
        scoreProgress.progress = score / 100f
        allTime.text = getString(R.string.all_time_score, allTimeScore)
    }

    private fun setupViews() = binding.apply {
        scoreProgress.progress = 1f
    }

    companion object {
        fun newInstance() = ScoreFragment()
    }
}
