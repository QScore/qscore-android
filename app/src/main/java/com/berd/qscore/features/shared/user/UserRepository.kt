package com.berd.qscore.features.shared.user

import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.api.models.QUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object UserRepository {
    var currentUser: QUser? = null
        private set

    private val userMap = hashMapOf<String, QUser>()

    suspend fun getCurrentUser(): QUser {
        val currentUser = Api.getCurrentUser()
        this.currentUser = currentUser
        return currentUser
    }

    suspend fun followUser(userId: String) {
        GlobalScope.launch { Api.followUser(userId) }
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
        GlobalScope.launch { Api.unfollowUser(userId) }
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
        val user = Api.getUser(userId)
        if (user != null) {
            userMap[user.userId] = user
        }
        return user
    }

    suspend fun searchUsers(query: String, limit: Int = 30): Api.UserListResult {
        return Api.searchUsers(query, limit).also { saveUsers(it.users) }
    }

    suspend fun searchUsersWithCursor(cursor: String): Api.UserListResult {
        return Api.searchUsersWithCursor(cursor).also { saveUsers(it.users) }
    }

    suspend fun getLeaderboardRange(start: Int, end: Int): List<QUser> {
        return Api.getLeaderboardRange(start, end).also { saveUsers(it) }
    }

    suspend fun getSocialLeaderboardRange(start: Int, end: Int): List<QUser> {
        return Api.getSocialLeaderboardRange(start, end).also { saveUsers(it) }
    }

    suspend fun getFollowers(userId: String): Api.UserListResult {
        return Api.getFollowers(userId).also { saveUsers(it.users) }
    }

    suspend fun getFollowersWithCursor(cursor: String): Api.UserListResult {
        return Api.getFollowersWithCursor(cursor).also { saveUsers(it.users) }
    }

    suspend fun getFollowedUsers(userId: String): Api.UserListResult {
        return Api.getFollowedUsers(userId).also { saveUsers(it.users) }
    }

    suspend fun getFollowedUsersWithCursor(cursor: String): Api.UserListResult {
        return Api.getFollowedUsersWithCursor(cursor).also { saveUsers(it.users) }
    }

    suspend fun updateUserInfo(username: String) {
        Api.updateUserInfo(username).also {
            currentUser = currentUser?.copy(
                username = username
            )
        }
    }

    suspend fun checkUsernameExists(username: String): Boolean {
        return Api.checkUsernameExists(username)
    }

    private fun saveUsers(users: List<QUser>) {
        users.forEach {
            userMap[it.userId] = it
        }
    }
}
