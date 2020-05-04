package com.berd.qscore.features.shared.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.*
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.fragment.UserFields
import com.berd.qscore.type.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.math.roundToInt


object Api {
    const val STAGE_URL = "https://tjeslxndo2.execute-api.us-east-1.amazonaws.com/dev/graphql"
    const val LOCAL_URL = "https://359960b3.ngrok.io/dev/graphql"

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

    suspend fun getLeaderboardRange(start: Int, end: Int): List<QUser> {
        val input = LeaderboardRangeInput(start, end)
        val query = GetLeaderboardRangeQuery(input)
        val result = apolloClient.query(query).call()
        return result.getLeaderboardRange.users.map {
            it.fragments.userFields.toQUser()
        }
    }

    suspend fun followUser(userId: String) {
        val input = FollowUserInput(userId)
        val mutation = FollowUserMutation(input)
        apolloClient.mutate(mutation).call()
    }

    suspend fun unfollowUser(userId: String) {
        val input = UnfollowUserInput(userId)
        val mutation = UnfollowUserMutation(input)
        apolloClient.mutate(mutation).call()
    }

    suspend fun getUser(userId: String): QUser? {
        val input = GetUserInput(userId)
        val query = GetUserQuery(input)
        val result = apolloClient.query(query).call()
        val user = result.getUser.user ?: return null
        return user.fragments.userFields.toQUser()
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
        return currentUser.fragments.userFields.toQUser()
    }

    suspend fun createGeofenceEvent(eventType: GeofenceEventType) {
        val input = CreateGeofenceEventInput(eventType)
        val mutation = CreateGeofenceEventMutation(input)
        apolloClient.mutate(mutation).call()
    }

    suspend fun searchUsers(query: String, limit: Int): List<QUser> {
        val input = SearchUsersInput(query, limit)
        val query = SearchUsersQuery(input)
        val result = apolloClient.query(query).call()
        return result.searchUsers.users.map {
            it.fragments.userFields.toQUser()
        }
    }

    suspend fun getFollowers(userId: String): List<QUser> {
        val input = GetFollowersInput(userId)
        val query = GetFollowersQuery(input)
        val result = apolloClient.query(query).call()
        return result.getFollowers.users.map {
            it.fragments.userFields.toQUser()
        }
    }

    suspend fun getFollowersWithCursor(cursor: String): List<QUser> {
        val input = GetFollowersWithCursorInput(cursor)
        val query = GetFollowersWithCursorQuery(input)
        val result = apolloClient.query(query).call()
        return result.getFollowersWithCursor.users.map {
            it.fragments.userFields.toQUser()
        }
    }

    suspend fun getFollowedUsers(userId: String): List<QUser> {
        val input = GetFollowedUsersInput(userId)
        val query = GetFollowedUsersQuery(input)
        val result = apolloClient.query(query).call()
        return result.getFollowedUsers.users.map {
            it.fragments.userFields.toQUser()
        }
    }

    suspend fun getFollowedUsersWithCursor(userId: String): List<QUser> {
        val input = GetFollowedUsersWithCursorInput(userId)
        val query = GetFollowedUsersWithCursorQuery(input)
        val result = apolloClient.query(query).call()
        return result.getFollowedUsersWithCursor.users.map {
            it.fragments.userFields.toQUser()
        }
    }

    private fun UserFields.toQUser() =
        QUser(
            userId = userId,
            username = username,
            score = score ?: 0.0,
            allTimeScore = (allTimeScore ?: 0.0).roundToInt().toString(),
            isCurrentUserFollowing = isCurrentUserFollowing ?: false,
            rank = rank?.toString(),
            avatar = avatar,
            followingCount = followingCount ?: 0,
            followerCount = followerCount ?: 0
        )

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
