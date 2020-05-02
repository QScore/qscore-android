package com.berd.qscore.features.shared.user

import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.features.shared.api.Api
import com.berd.qscore.features.shared.api.models.QUser
import timber.log.Timber

object UserRepository {
    var currentUser: QUser? = null
        private set

    private val userMap = hashMapOf<String, QUser>()

    suspend fun loadCurrentUser() {
        try {
            currentUser = Api.getCurrentUser()
        } catch (e: ApolloException) {
            Timber.d("Unable to load current user: $e")
        }
    }

    suspend fun followUser(userId: String) {
        Api.followUser(userId)
    }

    suspend fun unfollowUser(userId: String) {
        Api.unfollowUser(userId)
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

    suspend fun getLeaderboardRange(start: Int, end: Int): List<QUser> {
        val users = Api.getLeaderboardRange(start, end)
        users.map {
            if (userMap.containsKey(it.userId)) {
                val existingUser = userMap[it.userId]
                val updatedUser = existingUser?.copy(
                )
            }
        }
        return users
    }
}
