package com.berd.qscore.features.shared.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import com.berd.qscore.CreateGeofenceEventMutation
import com.berd.qscore.type.CreateGeofenceEventInput
import com.berd.qscore.utils.injection.Injector
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


object Api {

    private const val SERVER_URL = "https://tjeslxndo2.execute-api.us-east-1.amazonaws.com/dev/graphql"

    private val context = Injector.appContext
    private val apolloClient by lazy { buildApolloClient() }

    private fun buildApolloClient(): ApolloClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(FirebaseUserIdTokenInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

        return ApolloClient.builder()
            .serverUrl(SERVER_URL)
            .defaultHttpCachePolicy(HttpCachePolicy.CACHE_FIRST)
            .okHttpClient(okHttpClient)
            .build()
    }

    suspend fun createGeofenceEvent(input: CreateGeofenceEventInput) {
        val mutation = CreateGeofenceEventMutation(input)
        apolloClient.mutate(mutation).toDeferred().await()
    }
}
