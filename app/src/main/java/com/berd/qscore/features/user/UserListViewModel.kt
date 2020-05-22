package com.berd.qscore.features.user

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.utils.paging.PagedListCursorBuilder
import com.berd.qscore.utils.paging.PagedCursorResult
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.features.user.UserListActivity.UserListType
import com.berd.qscore.features.user.UserListActivity.UserListType.FOLLOWERS
import com.berd.qscore.features.user.UserListViewModel.UserListAction.*
import com.berd.qscore.utils.injection.Injector
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import timber.log.Timber

class UserListViewModel(private val handle: SavedStateHandle, private val userId: String, private val userListType: UserListType) :
    RxViewModelWithState<UserListViewModel.UserListAction, UserListViewModel.UserListState>(handle) {

    private val userRepository = Injector.userRepository

    override fun getInitialState() = UserListState()

    override fun updateState(action: UserListAction, state: UserListState) = when (action) {
        is SetLoading -> state.copy(isLoading = action.loading)
        is ShowNoUsersFound -> state.copy(noUsersFound = true)
        else -> state
    }

    sealed class UserListAction {
        class Initialize(val state: UserListState) : UserListAction()
        class SubmitPagedList(val pagedList: PagedList<QUser>) : UserListAction()
        class SetLoading(val loading: Boolean) : UserListAction()
        object ShowNoUsersFound : UserListAction()
    }

    @Parcelize
    data class UserListState(
        val isLoading: Boolean = false,
        val noUsersFound: Boolean = false
    ) : Parcelable

    fun onCreate() {
        action(Initialize(state))
        viewModelScope.launch {
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
        firstPageCall = { userId -> userRepository.getFollowedUsers(userId) },
        nextPageCall = { cursor -> userRepository.getFollowedUsersWithCursor(cursor) }
    )

    private suspend fun setupFollowersPagedList() = setupPagedList(
        firstPageCall = { userId -> userRepository.getFollowers(userId) },
        nextPageCall = { cursor -> userRepository.getFollowersWithCursor(cursor) }
    )

    private suspend fun setupPagedList(
        firstPageCall: suspend (userId: String) -> Api.UserListResult,
        nextPageCall: suspend (cursor: String) -> Api.UserListResult
    ): PagedList<QUser> {
        return PagedListCursorBuilder(
            limit = 30,
            onLoadFirstPage = {
                action(SetLoading(true))
                try {
                    val result = firstPageCall(userId)
                    PagedCursorResult<QUser>(result.users, result.nextCursor)
                } catch (e: ApolloException) {
                    Timber.d("Unable to search for users: $e")
                    PagedCursorResult<QUser>(emptyList(), null)
                } finally {
                    action(SetLoading(false))
                }
            },
            onLoadNextPage = { cursor ->
                try {
                    val result = nextPageCall(cursor)
                    PagedCursorResult<QUser>(result.users, result.nextCursor)
                } catch (e: ApolloException) {
                    Timber.d("Unable to search for users: $e")
                    PagedCursorResult<QUser>(emptyList(), null)
                }
            },
            onNoItemsLoaded = {
                action(ShowNoUsersFound)
            }
        ).build()
    }

}
