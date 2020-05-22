package com.berd.qscore.features.shared.user

import com.berd.qscore.features.geofence.GeofenceStatus
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.type.GeofenceEventType
import com.berd.qscore.utils.geofence.GeofenceHelper
import com.berd.qscore.utils.injection.Injector
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserRepository {
    var currentUser: QUser? = null
        private set

    private val api = Injector.api
    private val userMap = hashMapOf<String, QUser>()

    suspend fun createGeofenceEvent(status: GeofenceStatus) {
        api.createGeofenceEvent(status).also {
            currentUser = currentUser?.copy(
                geofenceStatus = status
            )
        }
    }

    suspend fun getCurrentUser(): QUser {
        val currentUser = api.getCurrentUser()
        this.currentUser = currentUser
        return currentUser
    }

    suspend fun followUser(userId: String) {
        GlobalScope.launch { api.followUser(userId) }
        val existing = userMap[userId]
        if (existing != null) {
            val updated = existing.copy(
                followerCount = existing.followerCount + 1,
                isCurrentUserFollowing = true
            )
            userMap[userId] = updated
        }

        currentUser = currentUser?.let {
            it.copy(
                followingCount = it.followingCount + 1
            )
        }
    }

    suspend fun unfollowUser(userId: String) {
        GlobalScope.launch { api.unfollowUser(userId) }
        val existing = userMap[userId]
        if (existing != null) {
            val updated = existing.copy(
                followerCount = existing.followerCount - 1,
                isCurrentUserFollowing = false
            )
            userMap[userId] = updated
        }
        currentUser = currentUser?.let {
            it.copy(
                followingCount = it.followingCount - 1
            )
        }
    }

    suspend fun getUser(userId: String): QUser? {
        if (userMap.contains(userId)) {
            return userMap[userId]
        }
        val user = api.getUser(userId)
        if (user != null) {
            userMap[user.userId] = user
        }
        return user
    }

    suspend fun searchUsers(query: String, limit: Int = 30): Api.UserListResult {
        return api.searchUsers(query, limit).also { saveUsers(it.users) }
    }

    suspend fun searchUsersWithCursor(cursor: String): Api.UserListResult {
        return api.searchUsersWithCursor(cursor).also { saveUsers(it.users) }
    }

    suspend fun getLeaderboardRange(start: Int, end: Int): List<QUser> {
        return api.getLeaderboardRange(start, end).also { saveUsers(it) }
    }

    suspend fun getSocialLeaderboardRange(start: Int, end: Int): List<QUser> {
        return api.getSocialLeaderboardRange(start, end).also { saveUsers(it) }
    }

    suspend fun getFollowers(userId: String): Api.UserListResult {
        return api.getFollowers(userId).also { saveUsers(it.users) }
    }

    suspend fun getFollowersWithCursor(cursor: String): Api.UserListResult {
        return api.getFollowersWithCursor(cursor).also { saveUsers(it.users) }
    }

    suspend fun getFollowedUsers(userId: String): Api.UserListResult {
        return api.getFollowedUsers(userId).also { saveUsers(it.users) }
    }

    suspend fun getFollowedUsersWithCursor(cursor: String): Api.UserListResult {
        return api.getFollowedUsersWithCursor(cursor).also { saveUsers(it.users) }
    }

    suspend fun updateUsername(username: String) {
        api.updateUserInfo(username).also {
            currentUser = currentUser?.copy(
                username = username
            )
        }
    }

    suspend fun updateAvatar(avatar: String) {
        api.updateUserInfo(avatar = avatar).also {
            currentUser = currentUser?.copy(
                avatar = avatar
            )
        }
    }

    suspend fun createUser(username: String) {
        api.createUser(username)
    }

    suspend fun checkUsernameExists(username: String): Boolean {
        return api.checkUsernameExists(username)
    }

    private fun saveUsers(users: List<QUser>) {
        users.forEach {
            userMap[it.userId] = it
        }
    }
}
