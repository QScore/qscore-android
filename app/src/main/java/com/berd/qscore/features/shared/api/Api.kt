package com.berd.qscore.features.shared.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import com.berd.qscore.CreateGeofenceEventMutation
import com.berd.qscore.CurrentUserQuery
import com.berd.qscore.UpdateUserInfoMutation
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.type.CreateGeofenceEventInput
import com.berd.qscore.type.UpdateUserInfoInput
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException


object Api {

    private const val SERVER_URL = "[STAGE_URL]"

    private val apolloClient by lazy { buildApolloClient() }

    private fun buildApolloClient(): ApolloClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(FirebaseUserIdTokenInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()

        return ApolloClient.builder()
            .serverUrl(SERVER_URL)
            .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST)
            .okHttpClient(okHttpClient)
            .build()
    }

    suspend fun updateUserInfo(input: UpdateUserInfoInput) {
        val mutation = UpdateUserInfoMutation(input)
        apolloClient.mutate(mutation).call()
    }

    suspend fun getCurrentUser(): QUser {
        val query = CurrentUserQuery()
        val result = apolloClient.query(query).call()
        val currentUser = result.currentUser.user
        return QUser(
            userId = currentUser.id,
            username = currentUser.username,
            score = currentUser.score ?: 0.0
        )
    }

    suspend fun createGeofenceEvent(input: CreateGeofenceEventInput) {
        val mutation = CreateGeofenceEventMutation(input)
        apolloClient.mutate(mutation).call()
    }

    private suspend fun <T : Any> ApolloQueryCall<T>.call(): T {
        val result = toDeferred().await()
        if (result.hasErrors()) {
            throw IOException(result.errors().toString())
        }
        return checkNotNull(result.data())
    }

    private suspend fun <T : Any> ApolloMutationCall<T>.call(): T? {
        val result = toDeferred().await()
        if (result.hasErrors()) {
            throw IOException(result.errors().toString())
        }
        return result.data()
    }
}
