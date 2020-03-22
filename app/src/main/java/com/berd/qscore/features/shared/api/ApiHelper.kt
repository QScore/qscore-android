package com.berd.qscore.features.shared.api

import com.amplifyframework.api.graphql.GraphQLResponse
import com.amplifyframework.api.graphql.MutationType
import com.amplifyframework.api.graphql.MutationType.*
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.ResultListener
import com.amplifyframework.core.model.Model
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ApiHelper {

    private val api by lazy { Amplify.API }

    suspend fun createEvent(event: SimpleEvent) = create(event, event.model)

    data class Response<T>(
        val data: T,
        val errors: List<String>
    ) {
        val hasErrors get() = errors.isNotEmpty()
    }

    private suspend inline fun <reified T : Model> update(simpleItem: SimpleModel<T>, item: T) = suspendCancellableCoroutine<Response<T>> {
        mutate(it, simpleItem, item, UPDATE)
    }

    private suspend inline fun <reified T : Model> delete(simpleItem: SimpleModel<T>, item: T) = suspendCancellableCoroutine<Response<T>> {
        mutate(it, simpleItem, item, DELETE)
    }

    private suspend inline fun <reified T : Model> create(simpleItem: SimpleModel<T>, item: T) = suspendCancellableCoroutine<Response<T>> {
        mutate(it, simpleItem, item, CREATE)
    }

    private inline fun <reified T : Model> mutate(
        cont: CancellableContinuation<Response<T>>,
        simpleItem: SimpleModel<T>,
        item: T,
        mutationType: MutationType
    ) {
        api.mutate(item, mutationType, responseListener(cont, simpleItem, item, mutationType))
    }

    private inline fun <reified T : Model> responseListener(
        cont: CancellableContinuation<Response<T>>,
        simpleItem: SimpleModel<T>,
        item: T,
        mutationType: MutationType
    ) =
        object : ResultListener<GraphQLResponse<T>> {
            val action by lazy {
                when (mutationType) {
                    CREATE -> "Create"
                    UPDATE -> "Update"
                    DELETE -> "Delete"
                }
            }

            val itemType by lazy {
                item::class.java.simpleName
            }

            override fun onResult(result: GraphQLResponse<T>) {
                val response = result.toResponse()
                if (response.errors.isNotEmpty()) {
                    Timber.d("$action $simpleItem completed with errors: ${response.errors}")
                } else {
                    Timber.d("$action $simpleItem completed successfully")
                }
                cont.resume(response)
            }

            override fun onError(error: Throwable) {
                Timber.e("$action $simpleItem failed: $error")
                cont.resumeWithException(error)
            }
        }

    private inline fun <reified T> GraphQLResponse<T>.toResponse() =
        Response<T>(
            data = data,
            errors = errors.map { it.message }
        )
}