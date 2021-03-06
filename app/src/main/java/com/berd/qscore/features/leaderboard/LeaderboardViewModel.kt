package com.berd.qscore.features.leaderboard

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.leaderboard.LeaderboardViewModel.LeaderboardAction.*
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.paging.PagedListOffsetBuilder
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.Serializable

enum class LeaderboardType : Serializable {
    SOCIAL,
    GLOBAL
}

class LeaderboardViewModel(handle: SavedStateHandle, private val leaderboardType: LeaderboardType) :
    RxViewModelWithState<LeaderboardViewModel.LeaderboardAction, LeaderboardViewModel.LeaderboardState>(handle) {

    private val userRepository by lazy { Injector.userRepository }

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
            is SetProgressShown -> state.copy(inProgress = action.visible).apply { pagedList = state.pagedList }
            is SubmitPagedList -> state.apply { pagedList = action.pagedList }
            else -> state
        }

    fun onViewCreated() {
        action(Initialize(state))
        if (state.pagedList == null) {
            action(SetProgressShown(true))
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
        return PagedListOffsetBuilder(
            pageSize = 30,
            onLoadFirstPage = ::doLoad,
            onLoadNextPage = ::doLoad
        ).build()
    }

    private suspend fun doLoad(offset: Int, limit: Int) =
        try {
            when (leaderboardType) {
                LeaderboardType.GLOBAL -> userRepository.getLeaderboardRange(offset, offset + limit)
                LeaderboardType.SOCIAL -> userRepository.getSocialLeaderboardRange(offset, offset + limit)
            }.also { action(SetProgressShown(false)) }
        } catch (e: ApolloException) {
            Timber.d("Unable to fetch leaderboard: $e")
            emptyList<QUser>()
        }
}
