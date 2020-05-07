package com.berd.qscore.features.user

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.geofence.GeofenceBroadcastReceiver
import com.berd.qscore.features.geofence.GeofenceStatus
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserRepository
import com.berd.qscore.features.shared.viewmodel.RxViewModel
import com.berd.qscore.features.user.UserFragment.ProfileType
import com.berd.qscore.features.user.UserViewModel.UserAction
import com.berd.qscore.features.user.UserViewModel.UserAction.*
import com.berd.qscore.features.user.UserViewModel.UserState
import com.berd.qscore.features.user.UserViewModel.UserState.Loading
import com.berd.qscore.features.user.UserViewModel.UserState.Ready
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.launch
import timber.log.Timber


class UserViewModel(private val profileType: ProfileType) : RxViewModel<UserAction, UserState>() {
    sealed class UserAction {
        class LaunchFollowingUserList(val userId: String) : UserAction()
        class LaunchFollowersUserList(val userId: String) : UserAction()
        class SetGeofenceStatus(val status: GeofenceStatus) : UserAction()
    }

    sealed class UserState {
        object Loading : UserState()
        class Ready(val user: QUser) : UserState()
    }

    fun onCreate() {
        UserRepository?.currentUser?.let {
            state = Ready(it)
        } ?: run {
            state = Loading
        }

        if (profileType is ProfileType.CurrentUser) {
            listenToGeofenceEvents()
        }
    }

    private fun listenToGeofenceEvents() {
        GeofenceBroadcastReceiver.events.subscribeBy(onNext = {
            action(SetGeofenceStatus(GeofenceStatus.HOME))
        }, onError = {
            Timber.d("Unable to listen to geofence events: $it")
        }).addTo(compositeDisposable)
    }

    fun onResume() {
        viewModelScope.launch {
            try {
                val user = when (profileType) {
                    is ProfileType.CurrentUser -> UserRepository.getCurrentUser()
                    is ProfileType.User -> UserRepository.getUser(profileType.userId)
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

    enum class FollowType {
        FOLLOW,
        UNFOLLOW
    }

    fun onFollowButtonClicked(userId: String, followType: FollowType) {
        viewModelScope.launch {
            if (followType == FollowType.FOLLOW) {
                UserRepository.followUser(userId)
            } else {
                UserRepository.unfollowUser(userId)
            }
            val updatedUser = UserRepository.getUser(userId)
            updatedUser?.let { state = Ready(it) }
                ?: Timber.d("Unable to update user after follow button clicked, no user found")
        }
    }

    fun onFollowersClicked() {
        val userId = profileType.userId
        action(LaunchFollowersUserList(userId))
    }

    fun onFollowingClicked() {
        val userId = profileType.userId
        action(LaunchFollowingUserList(userId))
    }

    fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            val geofenceStatus = GeofenceBroadcastReceiver.currentStatus
            action(SetGeofenceStatus(geofenceStatus))
        }
    }

    val ProfileType.userId
        get() = when (this) {
            ProfileType.CurrentUser -> UserRepository.currentUser?.userId
                ?: throw  Exception("Unable to launch user list activity, missing current user")
            is ProfileType.User -> userId
        }
}
