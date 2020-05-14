package com.berd.qscore.features.user

import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.utils.paging.PagedListCursorBuilder
import com.berd.qscore.utils.paging.PagedCursorResult
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelOld
import com.berd.qscore.features.user.UserListActivity.UserListType
import com.berd.qscore.features.user.UserListActivity.UserListType.FOLLOWERS
import com.berd.qscore.features.user.UserListViewModel.UserListAction.SubmitPagedList
import com.berd.qscore.features.user.UserListViewModel.UserListState.*
import kotlinx.coroutines.launch

class UserListViewModel(private val userId: String, private val userListType: UserListType) :
    RxViewModelOld<UserListViewModel.UserListAction, UserListViewModel.UserListState>() {
    fun onCreate() {
        viewModelScope.launch {
            val users =
                if (userListType == FOLLOWERS) {
                    val pagedList = setupFollowersPagedList()
                    action(SubmitPagedList(pagedList))
                } else {
                    val pagedList = setupFollowedUsersPagedList()
                    action(SubmitPagedList(pagedList))
                }
        }
    }

    private suspend fun setupFollowedUsersPagedList() = setupPagedList(
        firstPageCall = { userId -> UserRepository.getFollowedUsers(userId) },
        nextPageCall = { cursor -> UserRepository.getFollowedUsersWithCursor(cursor) }
    )

    private suspend fun setupFollowersPagedList() = setupPagedList(
        firstPageCall = { userId -> UserRepository.getFollowers(userId) },
        nextPageCall = { cursor -> UserRepository.getFollowersWithCursor(cursor) }
    )

    private suspend fun setupPagedList(
        firstPageCall: suspend (userId: String) -> Api.UserListResult,
        nextPageCall: suspend (cursor: String) -> Api.UserListResult
    ): PagedList<QUser> {
        return PagedListCursorBuilder(
            limit = 30,
            onLoadFirstPage = { limit ->
                state = Loading
                val result = firstPageCall(userId)
                state = Loaded
                PagedCursorResult(result.users, result.nextCursor)
            },
            onLoadNextPage = { cursor ->
                val result = nextPageCall(cursor)
                PagedCursorResult(result.users, result.nextCursor)
            },
            onNoItemsLoaded = {
                state = NoUsersFound
            }
        ).build()
    }

    sealed class UserListAction {
        class SubmitPagedList(val pagedList: PagedList<QUser>) : UserListAction()
    }

    sealed class UserListState {
        object Loading : UserListState()
        object Loaded : UserListState()
        object NoUsersFound : UserListState()
    }
}
