package com.berd.qscore.features.score

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.score.ScoreViewModel.ScoreAction
import com.berd.qscore.features.score.ScoreViewModel.ScoreState
import com.berd.qscore.features.score.ScoreViewModel.ScoreState.Loading
import com.berd.qscore.features.score.ScoreViewModel.ScoreState.Ready
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.launch
import timber.log.Timber


class ScoreViewModel : RxViewModel<ScoreAction, ScoreState>() {

    sealed class ScoreAction {
    }

    sealed class ScoreState {
        object Loading : ScoreState()
        class Ready(val score: Int, val allTimeScore: Int) : ScoreState()
    }

    fun onCreate() {
        state = Loading
    }

    fun onResume() {
        viewModelScope.launch {
            try {
                val user = Api.getCurrentUser()
                state = Ready(user.score.toInt(), user.allTimeScore.toInt())
            } catch (e: ApolloException) {
                Timber.d("Error getting score: $e")
            }
        }
    }
}
