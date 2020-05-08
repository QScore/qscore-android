package com.berd.qscore.features.leaderboard

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.Serializable

enum class LeaderboardType : Serializable {
    SOCIAL,
    GLOBAL
}

class LeaderboardViewModel(private val leaderboardType: LeaderboardType) :
    RxViewModel<LeaderboardViewModel.LeaderboardAction, LeaderboardViewModel.LeaderboardState>() {

    fun onViewCreated() {
        loadLeaderboard()
    }

    fun onRefresh() {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                val leaderboard = when (leaderboardType) {
                    LeaderboardType.GLOBAL -> UserRepository.getLeaderboardRange(0, 100)
                    LeaderboardType.SOCIAL -> UserRepository.getSocialLeaderboardRange(0, 100)
                }
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
