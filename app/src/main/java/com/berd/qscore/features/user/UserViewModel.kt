package com.berd.qscore.features.user

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.geofence.GeofenceBroadcastReceiver
import com.berd.qscore.features.geofence.GeofenceStatus
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.prefs.Prefs
import com.berd.qscore.features.shared.viewmodel.RxViewModelWithState
import com.berd.qscore.features.user.UserFragment.ProfileType
import com.berd.qscore.features.user.UserViewModel.UserAction
import com.berd.qscore.features.user.UserViewModel.UserAction.*
import com.berd.qscore.utils.analytics.Analytics
import com.berd.qscore.utils.injection.Injector
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber


class UserViewModel(private val handle: SavedStateHandle, private val profileType: ProfileType) :
    RxViewModelWithState<UserAction, UserViewModel.UserState>(handle) {
    sealed class UserAction {
        class LaunchFollowingUserList(val userId: String) : UserAction()
        class LaunchFollowersUserList(val userId: String) : UserAction()
        class SetGeofenceStatus(val status: GeofenceStatus) : UserAction()
        class DisplayUser(val user: QUser) : UserAction()
    }

    private val geofenceHelper = Injector.geofenceHelper
    private val userRepository = Injector.userRepository
    private val locationHelper = Injector.locationHelper

    override fun getInitialState() = UserState()

    override fun updateState(action: UserAction, state: UserState) = when (action) {
        is SetGeofenceStatus -> state.copy(geofenceStatus = action.status)
        is DisplayUser -> state.copy(user = action.user)
        else -> state
    }

    @Parcelize
    data class UserState(
        val geofenceStatus: GeofenceStatus = GeofenceStatus.HOME,
        val user: QUser? = null
    ) : Parcelable

    fun onCreate() {
        userRepository.currentUser?.let { action(DisplayUser(it)) }
        if (profileType is ProfileType.CurrentUser) {
            listenToGeofenceEvents()
        }
    }

    private fun listenToGeofenceEvents() {
        GeofenceBroadcastReceiver.events.subscribeBy(onNext = {
            action(SetGeofenceStatus(it))
        }, onError = {
            Timber.d("Unable to listen to geofence events: $it")
        }).addTo(compositeDisposable)
    }

    fun onResume() {
        viewModelScope.launch {
            try {
                if (profileType is ProfileType.CurrentUser) {
                    async { updateGeofenceStatus() }
                }
                val user = when (profileType) {
                    is ProfileType.CurrentUser -> userRepository.getCurrentUser()
                    is ProfileType.User -> userRepository.getUser(profileType.userId)
                        ?: throw ApolloException("No user found for id: ${profileType.userId}")
                }
                action(DisplayUser(user))
            } catch (e: ApolloException) {
                Timber.d("Unable to fetch user: $e")
            }
        }
    }

    private suspend fun updateGeofenceStatus() {
        val currentLocation = locationHelper.fetchCurrentLocation()
        val geofenceLocation = Prefs.userLocation
        if (currentLocation != null && geofenceLocation != null) {
            val isInsideGeofence = geofenceHelper.checkGeofence(
                geofenceLocation = geofenceLocation,
                userLocation = currentLocation
            )
            val geofenceEvent = if (isInsideGeofence) GeofenceStatus.HOME else GeofenceStatus.AWAY
            userRepository.createGeofenceEvent(geofenceEvent)
            action(SetGeofenceStatus(geofenceEvent))
        }
    }

    fun onGifAvatarSelected(url: String) {
        viewModelScope.launch {
            try {
                userRepository.updateAvatar(avatar = url)
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
            try {
                if (followType == FollowType.FOLLOW) {
                    userRepository.followUser(userId)
                } else {
                    userRepository.unfollowUser(userId)
                }
                val updatedUser = userRepository.getUser(userId)
                updatedUser?.let { action(DisplayUser(it)) }
                    ?: Timber.d("Unable to update user after follow button clicked, no user found")
            } catch (e: ApolloException) {
                Timber.d("Unable to follow user: $e")
            }
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
            userRepository.currentUser?.geofenceStatus?.let { action(SetGeofenceStatus(it)) }
        }
    }

    fun onAvatarClicked() {
        Analytics.trackAvatarClicked()
    }

    val ProfileType.userId
        get() = when (this) {
            ProfileType.CurrentUser -> userRepository.currentUser?.userId
                ?: throw  Exception("Unable to launch user list activity, missing current user")
            is ProfileType.User -> userId
        }
}
