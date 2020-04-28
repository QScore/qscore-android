package com.berd.qscore.features.search

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.type.SearchUsersInput
import kotlinx.coroutines.launch
import timber.log.Timber


class SearchViewModel : RxViewModel<SearchViewModel.SearchAction, SearchViewModel.SearchState>() {
    fun onSearch(query: String) {
        if (query.isEmpty()) {
            state = SearchState.EmptyResults
        }
        viewModelScope.launch {
            try {
                val users = Api.searchUsers(SearchUsersInput(query))
                state = SearchState.UsersLoaded(users)
            } catch (e: ApolloException) {
                Timber.d("Unable to search users: $e")
            }
        }
    }

    sealed class SearchAction {
        object ShowProgress : SearchAction()
        object HideProgress : SearchAction()
    }

    sealed class SearchState {
        class UsersLoaded(val users: List<QUser>) : SearchState()
        object EmptyResults : SearchState()
    }


}
