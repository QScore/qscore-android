package com.berd.qscore.utils.paging

import androidx.paging.PagedList
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


data class PagedListCursorBuilder<T>(
    val limit: Int,
    val onLoadFirstPage: suspend (limit: Int) -> PagedCursorResult<T>,
    val onLoadNextPage: (suspend (cursor: String) -> PagedCursorResult<T>)? = null,
    val onNoItemsLoaded: (() -> Unit)? = null
) {
    suspend fun build(): PagedList<T> = suspendCancellableCoroutine { cont ->
        val pagedListHelper = object : PagedListCursorHelper<T>() {
            override fun loadFirstPage(limit: Int) = runBlocking {
                val result = onLoadFirstPage(limit)
                PagedCursorResult(result.items, result.nextCursor)
            }

            override fun loadNextPage(cursor: String) = runBlocking {
                onLoadNextPage?.let { loadNextPage ->
                    val result = loadNextPage(cursor)
                    PagedCursorResult<T>(result.items, result.nextCursor)
                } ?: run {
                    PagedCursorResult<T>(emptyList(), null)
                }
            }

            override fun onNoItemsLoaded() {
                onNoItemsLoaded?.invoke()
            }
        }

        pagedListHelper.buildObservable()
            .take(1)
            .subscribeBy(onNext = {
                cont.resume(it)
            }, onError = {
                Timber.d("Unable to build paged list: $it")
                cont.resumeWithException(it)
            })
    }
}
