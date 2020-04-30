package com.berd.qscore.features.shared.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.berd.qscore.CreateGeofenceEventMutation
import com.berd.qscore.CurrentUserQuery
import com.berd.qscore.SearchUsersQuery
import com.berd.qscore.UpdateUserInfoMutation
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.type.CreateGeofenceEventInput
import com.berd.qscore.type.GeofenceEventType
import com.berd.qscore.type.SearchUsersInput
import com.berd.qscore.type.UpdateUserInfoInput
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.math.roundToInt


object Api {
    const val STAGE_URL = "https://tjeslxndo2.execute-api.us-east-1.amazonaws.com/dev/graphql"
    const val LOCAL_URL = "https://711f6c36.ngrok.io/dev/graphql"

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

    suspend fun updateUserInfo(username: String) {
        val input = UpdateUserInfoInput(username)
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
            rank = currentUser.rank?.toString() ?: "Unknown"
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
                rank = "Unknown"
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
