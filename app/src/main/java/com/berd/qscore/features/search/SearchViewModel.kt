package com.berd.qscore.features.search

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.search.SearchViewModel.SearchState.*
import com.berd.qscore.features.shared.api.models.QUser
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
                val users = UserRepository.searchUsers(query)
                state = if (users.isEmpty()) {
                    EmptyResults
                } else {
                    UsersLoaded(users)
                }
            } catch (e: ApolloException) {
                Timber.d("Unable to search users: $e")
                state = Error
            }
        }
    }

    sealed class SearchAction {
    }

    sealed class SearchState {
        class UsersLoaded(val users: List<QUser>) : SearchState()
        object Loading : SearchState()
        object EmptyResults : SearchState()
        object Error : SearchState()
    }


}
