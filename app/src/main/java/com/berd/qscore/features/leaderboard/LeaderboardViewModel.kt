package com.berd.qscore.features.leaderboard

import com.berd.qscore.features.shared.viewmodel.RxViewModel


class LeaderboardViewModel : RxViewModel<LeaderboardViewModel.LeaderboardAction, LeaderboardViewModel.LeaderboardState>() {
    sealed class LeaderboardAction {
    }

    sealed class LeaderboardState {
    }


}
