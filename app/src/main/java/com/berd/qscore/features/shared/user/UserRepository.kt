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

    suspend fun searchUsers(query: String, limit: Int = 30): List<QUser> {
        val users = Api.searchUsers(query, limit)
        users.forEach {
            userMap[it.userId] = it
        }
        return users
    }

    suspend fun getLeaderboardRange(start: Int, end: Int): List<QUser> {
        val users = Api.getLeaderboardRange(start, end)
        users.map {
            userMap[it.userId] = it
        }
        return users
    }
}
