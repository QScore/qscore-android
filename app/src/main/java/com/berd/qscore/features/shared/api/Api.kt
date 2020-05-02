package com.berd.qscore.features.shared.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.*
import com.berd.qscore.features.shared.api.models.QLeaderboardScore
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.type.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.math.roundToInt


object Api {
    const val STAGE_URL = "https://tjeslxndo2.execute-api.us-east-1.amazonaws.com/dev/graphql"
    const val LOCAL_URL = "https://6d02c50b.ngrok.io/dev/graphql"

    private val apolloClient by lazy { buildApolloClient() }

    private fun buildApolloClient(): ApolloClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(FirebaseUserIdTokenInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()

        return ApolloClient.builder()
            .serverUrl(LOCAL_URL)
            .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST)
            .okHttpClient(okHttpClient)
            .build()
    }

    suspend fun getLeaderboardRange(start: Int, end: Int): List<QLeaderboardScore> {
        val input = LeaderboardRangeInput(start, end)
        val query = GetLeaderboardRangeQuery(input)
        val result = apolloClient.query(query).call()
        return result.getLeaderboardRange.leaderboardScores.map {
            QLeaderboardScore(
                userId = it.user.userId,
                username = it.user.username,
                rank = it.rank,
                score = it.score,
                avatar = it.user.avatar
            )
        }
    }

    suspend fun getUser(userId: String): QUser? {
        val input = GetUserInput(userId)
        val query = GetUserQuery(input)
        val result = apolloClient.query(query).call()
        val user = result.getUser.user ?: return null
        return QUser(
            userId = user.userId,
            username = user.username,
            score = user.score ?: 0.0,
            allTimeScore = (user.allTimeScore ?: 0.0).roundToInt().toString(),
            followerCount = user.followerCount ?: 0,
            followingCount = user.followingCount ?: 0,
            rank = user.rank?.toString() ?: "Unknown",
            avatar = user.avatar
        )
    }

    suspend fun createUser(username: String) {
        val input = CreateUserInput(username)
        val mutation = CreateUserMutation(input)
        apolloClient.mutate(mutation).call()
    }

    suspend fun updateUserInfo(username: String? = null, avatar: String? = null) {
        val input = UpdateUserInfoInput(Input.optional(username), Input.optional(avatar))
        val mutation = UpdateUserInfoMutation(input)
        apolloClient.mutate(mutation).call()
    }

    suspend fun getCurrentUser(): QUser {
        val query = CurrentUserQuery()
        val result = apolloClient.query(query).call()
        val currentUser = result.currentUser.user
        return QUser(
            userId = currentUser.userId,
            username = currentUser.username,
            score = currentUser.score ?: 0.0,
            allTimeScore = (currentUser.allTimeScore ?: 0.0).roundToInt().toString(),
            followerCount = currentUser.followerCount ?: 0,
            followingCount = currentUser.followingCount ?: 0,
            rank = currentUser.rank?.toString() ?: "Unknown",
            avatar = currentUser.avatar
        )
    }

    suspend fun createGeofenceEvent(eventType: GeofenceEventType) {
        val input = CreateGeofenceEventInput(eventType)
        val mutation = CreateGeofenceEventMutation(input)
        apolloClient.mutate(mutation).call()
    }

    suspend fun searchUsers(query: String, limit: Int = 30): List<QUser> {
        val input = SearchUsersInput(query, limit)
        val query = SearchUsersQuery(input)
        val result = apolloClient.query(query).call()
        return result.searchUsers.users.map {
            QUser(
                userId = it.userId,
                username = it.username,
                score = it.score ?: 0.0,
                allTimeScore = (it.allTimeScore ?: 0.0).roundToInt().toString(),
                isCurrentUserFollowing = it.isCurrentUserFollowing ?: false,
                rank = "Unknown",
                avatar = it.avatar,
                followingCount = it.followingCount ?: 0,
                followerCount = it.followingCount ?: 0
            )
        }
    }

    private suspend fun <T : Any> ApolloQueryCall<T>.call(): T {
        val result = toDeferred().await()
        if (result.hasErrors() || result.data() == null) {
            throw ApolloException(result.errors().toString())
        }
        return checkNotNull(result.data())
    }

    private suspend fun <T : Any> ApolloMutationCall<T>.call(): T {
        val result = toDeferred().await()
        if (result.hasErrors() || result.data() == null) {
            throw ApolloException(result.errors().toString())
        }
        return checkNotNull(result.data())
    }
}
