package com.berd.qscore.utils.paging

import androidx.paging.PagedList
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


data class PagedListOffsetBuilder<T>(
    val pageSize: Int,
    val onLoadFirstPage: suspend (offset: Int, limit: Int) -> List<T>,
    val onLoadNextPage: (suspend (offset: Int, limit: Int) -> List<T>)? = null,
    val onNoItemsLoaded: (() -> Unit)? = null
) {
    suspend fun build(): PagedList<T> = suspendCancellableCoroutine { cont ->
        val pagedListHelper = object : PagedListOffsetHelper<T>(pageSize) {
            override fun loadFirstPage(offset: Int, limit: Int): List<T> = runBlocking {
                onLoadFirstPage(offset, limit)
            }

            override fun loadNextPage(offset: Int, limit: Int): List<T> = runBlocking {
                onLoadNextPage?.let { loadNextPage ->
                    loadNextPage(offset, limit)
                } ?: run {
                    emptyList<T>()
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
