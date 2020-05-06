package com.berd.qscore.features.search

import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.search.SearchViewModel.SearchAction.SubmitPagedList
import com.berd.qscore.features.search.SearchViewModel.SearchState.*
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.PagedListBuilder
import com.berd.qscore.features.shared.user.PagedResult
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel : RxViewModel<SearchViewModel.SearchAction, SearchViewModel.SearchState>() {
    private var searchJob: Job? = null

    fun onSearch(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            return
        }
        searchJob = viewModelScope.launch {
            try {
                state = Loading
                val pagedList = buildPagedList(query)
                action(SubmitPagedList(pagedList))
            } catch (e: ApolloException) {
                Timber.d("Unable to search users: $e")
                state = Error
            }
        }
    }

    private suspend fun buildPagedList(query: String): PagedList<QUser> {
        val builder = PagedListBuilder(
            limit = 30,
            onLoadFirstPage = { limit ->
                state = Loading
                val result = UserRepository.searchUsers(query, limit)
                state = Loaded
                PagedResult(result.users, result.nextCursor)
            },
            onLoadNextPage = { cursor ->
                val result = UserRepository.searchUsersWithCursor(cursor)
                PagedResult(result.users, result.nextCursor)
            }
        )
        return builder.build()
    }

    sealed class SearchAction {
        class SubmitPagedList(val pagedList: PagedList<QUser>) : SearchAction()
    }

    sealed class SearchState {
        object Loaded : SearchState()
        object Loading : SearchState()
        object EmptyResults : SearchState()
        object Error : SearchState()
    }
}
