package com.berd.qscore.features.friends

import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.friends.AddFriendsViewModel.Action
import com.berd.qscore.features.friends.AddFriendsViewModel.State
import com.berd.qscore.features.friends.AddFriendsViewModel.State.FieldsUpdated
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import kotlinx.coroutines.launch

class AddFriendsViewModel : RxViewModel<Action, State>() {

    sealed class Action {
    }

    sealed class State {
        object InProgress : State()
        object Ready : State()
        object ConnectionError : State()
        object FieldsUpdated : State()
    }

    fun onFieldsUpdated(username: String) = viewModelScope.launch {
        state = FieldsUpdated
    }
}