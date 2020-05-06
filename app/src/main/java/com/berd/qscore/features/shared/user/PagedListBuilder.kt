package com.berd.qscore.features.shared.user

import androidx.paging.PagedList
import com.berd.qscore.utils.paging.PagedListHelper
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


data class PagedListBuilder<T>(
    val limit: Int,
    val onLoadFirstPage: suspend (limit: Int) -> PagedResult<T>,
    val onLoadNextPage: (suspend (cursor: String) -> PagedResult<T>)? = null,
    val onNoItemsLoaded: (() -> Unit)? = null
) {
    suspend fun build(): PagedList<T> = suspendCancellableCoroutine { cont ->
        val pagedListHelper = object : PagedListHelper<T>() {
            override fun loadFirstPage(limit: Int) = runBlocking {
                val result = onLoadFirstPage(limit)
                PagedResult(result.items, result.nextCursor)
            }

            override fun loadNextPage(cursor: String) = runBlocking {
                onLoadNextPage?.let { loadNextPage ->
                    val result = loadNextPage(cursor)
                    PagedResult<T>(result.items, result.nextCursor)
                } ?: run {
                    PagedResult<T>(emptyList(), null)
                }
            }

            override fun onNoItemsLoaded() {
                onNoItemsLoaded?.let {
                    onNoItemsLoaded()
                }
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
