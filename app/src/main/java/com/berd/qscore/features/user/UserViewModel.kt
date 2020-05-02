package com.berd.qscore.features.user

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.features.user.UserFragment.ProfileType
import com.berd.qscore.features.user.UserViewModel.ScoreAction
import com.berd.qscore.features.user.UserViewModel.ScoreState
import com.berd.qscore.features.user.UserViewModel.ScoreState.Loading
import com.berd.qscore.features.user.UserViewModel.ScoreState.Ready
import kotlinx.coroutines.launch
import timber.log.Timber


class UserViewModel(private val profileType: ProfileType) : RxViewModel<ScoreAction, ScoreState>() {

    sealed class ScoreAction {
    }

    sealed class ScoreState {
        object Loading : ScoreState()
        class Ready(val user: QUser) : ScoreState()
    }

    fun onCreate() {
        state = Loading
    }

    fun onResume() {
        viewModelScope.launch {
            try {
                val user = when (profileType) {
                    is ProfileType.CurrentUser -> Api.getCurrentUser()
                    is ProfileType.User -> Api.getUser(profileType.userId)
                        ?: throw ApolloException("No user found for id: ${profileType.userId}")
                }
                state = Ready(user)
            } catch (e: ApolloException) {
                Timber.d("Error getting score: $e")
            }
        }
    }

    fun onGifAvatarSelected(url: String) {
        viewModelScope.launch {
            try {
                Api.updateUserInfo(avatar = url)
            } catch (e: ApolloException) {
                Timber.d("Unable to update avatar: $e")
            }
        }
    }

    fun onRefresh() {
        onResume()
    }

    fun onFollowButtonClicked(userId: String) {
        viewModelScope.launch {
            UserRepository.followUser(userId)
        }
    }
}
