package com.berd.qscore.features.leaderboard

import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.utils.paging.PagedListOffsetBuilder
import kotlinx.coroutines.launch
import java.io.Serializable

enum class LeaderboardType : Serializable {
    SOCIAL,
    GLOBAL
}

class LeaderboardViewModel(private val leaderboardType: LeaderboardType) :
    RxViewModel<LeaderboardViewModel.LeaderboardAction, LeaderboardViewModel.LeaderboardState>() {

    fun onViewCreated() {
        setupPagedList()
    }

    fun onRefresh() {
        setupPagedList()
    }

    private fun setupPagedList() {
        viewModelScope.launch {
            val pagedList = buildPagedList()
            action(LeaderboardAction.SubmitPagedList(pagedList))
        }
    }

    private suspend fun buildPagedList(): PagedList<QUser> {
        val builder = PagedListOffsetBuilder(
            pageSize = 30,
            onLoadFirstPage = { offset, limit ->
                when (leaderboardType) {
                    LeaderboardType.GLOBAL -> UserRepository.getLeaderboardRange(offset, limit)
                    LeaderboardType.SOCIAL -> UserRepository.getSocialLeaderboardRange(offset, limit)
                }.also { state = LeaderboardState.Loaded }
            },
            onLoadNextPage = { offset, limit ->
                when (leaderboardType) {
                    LeaderboardType.GLOBAL -> UserRepository.getLeaderboardRange(offset, limit)
                    LeaderboardType.SOCIAL -> UserRepository.getSocialLeaderboardRange(offset, limit)
                }.also { state = LeaderboardState.Loaded }
            }
        )
        return builder.build()
    }

    sealed class LeaderboardAction {
        class SubmitPagedList(val pagedList: PagedList<QUser>) : LeaderboardAction()
    }

    sealed class LeaderboardState {
        object Loaded : LeaderboardState()
    }


}
