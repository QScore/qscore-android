package com.berd.qscore.features.leaderboard

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.berd.qscore.features.leaderboard.LeaderboardViewModel.LeaderboardAction.*
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.utils.paging.PagedListOffsetBuilder
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import java.io.Serializable

enum class LeaderboardType : Serializable {
    SOCIAL,
    GLOBAL
}

class LeaderboardViewModel(handle: SavedStateHandle, private val leaderboardType: LeaderboardType) :
    RxViewModelWithState<LeaderboardViewModel.LeaderboardAction, LeaderboardViewModel.LeaderboardState>(handle) {

    @Parcelize
    data class LeaderboardState(
        val inProgress: Boolean = true
    ) : Parcelable {
        @IgnoredOnParcel
        var pagedList: PagedList<QUser>? = null
    }

    override fun getInitialState() = LeaderboardState()

    sealed class LeaderboardAction {
        class Initialize(val state: LeaderboardState) : LeaderboardAction()
        class SubmitPagedList(val pagedList: PagedList<QUser>) : LeaderboardAction()
        class SetProgressShown(val visible: Boolean) : LeaderboardAction()
    }

    override fun updateState(action: LeaderboardAction, state: LeaderboardState) =
        when (action) {
            is SetProgressShown -> state.copy(inProgress = action.visible)
            is SubmitPagedList -> state.apply { pagedList = action.pagedList }
            else -> state
        }

    fun onViewCreated() {
        action(Initialize(state))
        if (state.inProgress) {
            setupPagedList()
        }
    }

    fun onRefresh() {
        setupPagedList()
    }

    private fun setupPagedList() {
        viewModelScope.launch {
            val pagedList = buildPagedList()
            action(SubmitPagedList(pagedList))
        }
    }

    private suspend fun buildPagedList(): PagedList<QUser> {
        val builder = PagedListOffsetBuilder(
            pageSize = 30,
            onLoadFirstPage = { offset, limit ->
                when (leaderboardType) {
                    LeaderboardType.GLOBAL -> UserRepository.getLeaderboardRange(offset, limit)
                    LeaderboardType.SOCIAL -> UserRepository.getSocialLeaderboardRange(offset, limit)
                }.also { action(SetProgressShown(false)) }
            },
            onLoadNextPage = { offset, limit ->
                when (leaderboardType) {
                    LeaderboardType.GLOBAL -> UserRepository.getLeaderboardRange(offset, limit)
                    LeaderboardType.SOCIAL -> UserRepository.getSocialLeaderboardRange(offset, limit)
                }.also { action(SetProgressShown(false)) }
            }
        )
        return builder.build()
    }
}
