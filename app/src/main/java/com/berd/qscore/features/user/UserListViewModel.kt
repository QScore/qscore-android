package com.berd.qscore.features.user

import androidx.lifecycle.viewModelScope
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.features.user.UserListActivity.UserListType
import com.berd.qscore.features.user.UserListActivity.UserListType.FOLLOWERS
import kotlinx.coroutines.launch

class UserListViewModel(private val userId: String, private val userListType: UserListType) :
    RxViewModel<UserListViewModel.UserListAction, UserListViewModel.UserListState>() {
    fun onCreate() {
        viewModelScope.launch {
            val users =
                if (userListType == FOLLOWERS) {
                    UserRepository.getFollowers(userId)
                } else {
                    UserRepository.getFollowedUsers(userId)
                }
        }
    }

    sealed class UserListAction {

    }

    sealed class UserListState {
        class Ready(val users: List<QUser>) : UserListState()
    }


}
