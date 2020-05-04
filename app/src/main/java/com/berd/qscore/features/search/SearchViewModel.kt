package com.berd.qscore.features.search

import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.search.SearchViewModel.SearchState.*
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.utils.paging.PagedListHelper
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
                buildPagedList(query)
            } catch (e: ApolloException) {
                Timber.d("Unable to search users: $e")
                state = Error
            }
        }
    }

    private fun buildPagedList(query: String) {
        val pagedListHelper = object : PagedListHelper<QUser>() {
            override fun loadFirstPage(limit: Int): LoadResult = runBlocking {
                state = Loading
                val result = UserRepository.searchUsers(query, limit)
                state = Loaded
                LoadResult(result.users, result.nextCursor)
            }

            override fun loadNextPage(cursor: String): LoadResult = runBlocking {
                val result = UserRepository.searchUsersWithCursor(cursor)
                LoadResult(result.users, result.nextCursor)
            }

            override fun onNoItemsLoaded() {
                state = EmptyResults
            }
        }
        pagedListHelper.buildObservable()
            .take(1)
            .subscribeBy(onNext = {
                action(SearchAction.SubmitPagedList(it))
            }, onError = {
                Timber.d("Unable to build paged list: $it")
            }).addTo(compositeDisposable)
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
