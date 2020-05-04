package com.berd.qscore.utils.paging

import androidx.lifecycle.LiveData
import androidx.paging.*
import io.reactivex.Observable

abstract class PagedListHelper<T>(
    private val pageSize: Int = 30,
    private val enablePlaceHolders: Boolean = false
) {

    private val factory = object : DataSource.Factory<String, T>() {
        private var isFinished = false
        override fun create(): DataSource<String, T> {
            return object : PageKeyedDataSource<String, T>() {
                override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, T>) {
                    val result = loadFirstPage(params.requestedLoadSize)
                    callback.onResult(result.items, null, result.nextKey)
                    if (result.items.size < params.requestedLoadSize) {
                        isFinished = true
                    }
                }

                override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, T>) {
                    if (isFinished) {
                        callback.onResult(arrayListOf(), null)
                        return
                    }
                    val result = loadNextPage(params.key)
                    callback.onResult(result.items, result.nextKey)
                    if (result.items.size < params.requestedLoadSize) {
                        isFinished = true
                    }
                }

                override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, T>) {
                }
            }
        }
    }

    abstract fun loadFirstPage(limit: Int): LoadResult

    open fun loadNextPage(nextKey: String): LoadResult {
        return LoadResult(emptyList(), null)
    }

    inner class LoadResult(val items: List<T>, val nextKey: String?)

    open fun onNoItemsLoaded() {
        //optional override
    }

    fun buildObservable(): Observable<PagedList<T>> =
        RxPagedListBuilder(factory, buildConfig())
            .setBoundaryCallback(object : PagedList.BoundaryCallback<T>() {
                override fun onZeroItemsLoaded() {
                    onNoItemsLoaded()
                }
            })
            .buildObservable()

    fun buildLiveData(): LiveData<PagedList<T>> =
        LivePagedListBuilder(factory, buildConfig())
            .setBoundaryCallback(object : PagedList.BoundaryCallback<T>() {
                override fun onZeroItemsLoaded() {
                    onNoItemsLoaded()
                }
            })
            .build()

    private fun buildConfig(): PagedList.Config =
        PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setEnablePlaceholders(enablePlaceHolders)
            .build()
}

