package com.berd.qscore.features.search

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.search.SearchViewModel.SearchAction.SubmitPagedList
import com.berd.qscore.features.search.SearchViewModel.SearchAction.UpdateLoadingState
import com.berd.qscore.features.search.SearchViewModel.SearchState.LoadingState
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.utils.analytics.Analytics
import com.berd.qscore.utils.injection.Injector
import com.berd.qscore.utils.paging.PagedCursorResult
import com.berd.qscore.utils.paging.PagedListCursorBuilder
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel(handle: SavedStateHandle) : RxViewModelWithState<SearchViewModel.SearchAction, SearchViewModel.SearchState>(handle) {
    private var searchJob: Job? = null
    private val userRepository by lazy { Injector.userRepository }

    @Parcelize
    data class SearchState(
        val loadingState: LoadingState = LoadingState.READY
    ) : Parcelable {
        var pagedList: PagedList<QUser>? = null

        enum class LoadingState {
            READY,
            LOADING,
            LOADED,
            EMPTY_RESULTS,
            ERROR
        }
    }

    sealed class SearchAction {
        class Initialize(val state: SearchState) : SearchAction()
        class SubmitPagedList(val pagedList: PagedList<QUser>) : SearchAction()
        class UpdateLoadingState(val loadingState: LoadingState) : SearchAction()
    }

    override fun getInitialState() = SearchState()

    override fun updateState(action: SearchAction, state: SearchState) =
        when (action) {
            is SubmitPagedList -> state.apply { pagedList = action.pagedList }
            is UpdateLoadingState -> state.copy(loadingState = action.loadingState).apply { pagedList = state.pagedList }
            else -> state
        }

    fun onViewCreated() {
        action(SearchAction.Initialize(state))
    }

    fun onSearch(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            return
        }
        Analytics.trackSearch(query)
        searchJob = viewModelScope.launch {
            try {
                val pagedList = buildPagedList(query)
                action(SubmitPagedList(pagedList))
            } catch (e: ApolloException) {
                Timber.d("Unable to search users: $e")
                action(UpdateLoadingState(LoadingState.ERROR))
            }
        }
    }

    private suspend fun buildPagedList(query: String): PagedList<QUser> {
        action(UpdateLoadingState(LoadingState.LOADING))
        val builder = PagedListCursorBuilder(
            limit = 30,
            onLoadFirstPage = { limit ->
                try {
                    val result = userRepository.searchUsers(query, limit)
                    action(UpdateLoadingState(LoadingState.LOADED))
                    PagedCursorResult<QUser>(result.users, result.nextCursor)
                } catch (e: ApolloException) {
                    Timber.d("Unable to search for users: $e")
                    PagedCursorResult<QUser>(emptyList(), null)
                }
            },
            onLoadNextPage = { cursor ->
                try {
                    val result = userRepository.searchUsersWithCursor(cursor)
                    PagedCursorResult(result.users, result.nextCursor)
                } catch (e: ApolloException) {
                    Timber.d("Unable to search for users: $e")
                    PagedCursorResult<QUser>(emptyList(), null)
                }
            },
            onNoItemsLoaded = {
                action(UpdateLoadingState(LoadingState.EMPTY_RESULTS))
            }
        )
        return builder.build()
    }
}
