package com.berd.qscore.features.leaderboard

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.launch
import timber.log.Timber


class LeaderboardViewModel : RxViewModel<LeaderboardViewModel.LeaderboardAction, LeaderboardViewModel.LeaderboardState>() {
    fun onViewCreated() {
        loadLeaderboard()
    }

    fun onRefresh() {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                val leaderboard = UserRepository.getLeaderboardRange(0, 100)
                state = LeaderboardState.Ready(leaderboard)
            } catch (e: ApolloException) {
                Timber.d("Unable to fetch leaderboard range: $e")
            }
        }
    }

    sealed class LeaderboardAction {
    }

    sealed class LeaderboardState {
        class Ready(val leaderboard: List<QUser>) : LeaderboardState()
    }


}
