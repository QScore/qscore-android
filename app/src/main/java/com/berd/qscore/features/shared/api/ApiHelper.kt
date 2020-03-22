package com.berd.qscore.features.shared.api

import com.amplifyframework.api.graphql.GraphQLResponse
import com.amplifyframework.api.graphql.MutationType
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.ResultListener
import com.amplifyframework.core.model.Model
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ApiHelper {

    val api by lazy { Amplify.API }

    data class Response<T>(
        val data: T,
        val errors: List<String>
    )

    suspend inline fun <reified T : Model> update(item: T) = suspendCancellableCoroutine<Response<T>> {
        api.mutate(item, MutationType.UPDATE, responseListener(it))
    }

    suspend inline fun <reified T : Model> delete(item: T) = suspendCancellableCoroutine<Response<T>> {
        api.mutate(item, MutationType.DELETE, responseListener(it))
    }

    suspend inline fun <reified T : Model> create(item: T) = suspendCancellableCoroutine<Response<T>> {
        api.mutate(item, MutationType.CREATE, responseListener(it))
    }

    inline fun <reified T> responseListener(cont: CancellableContinuation<Response<T>>) =
        object : ResultListener<GraphQLResponse<T>> {
            override fun onResult(result: GraphQLResponse<T>) {
                val response = result.toResponse()
                Timber.d("Result: ${result.toResponse()}")
                cont.resume(response)
            }

            override fun onError(error: Throwable) {
                Timber.e("Failed: $error")
                cont.resumeWithException(error)
            }
        }

    inline fun <reified T> GraphQLResponse<T>.toResponse() =
        Response<T>(
            data = data,
            errors = errors.map { it.message }
        )
}