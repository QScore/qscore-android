package com.berd.qscore.utils.paging

import androidx.lifecycle.LiveData
import androidx.paging.*
import io.reactivex.Observable

abstract class PagedListOffsetHelper<T>(
    private val pageSize: Int = 30
) {

    private val factory = object : DataSource.Factory<Int, T>() {
        private var isFinished = false
        override fun create(): DataSource<Int, T> {
            return object : PositionalDataSource<T>() {
                override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
                    val offset = params.requestedStartPosition
                    val limit = params.pageSize
                    val result = loadFirstPage(
                        offset = offset,
                        limit = limit
                    )
                    callback.onResult(result, offset)
                    if (result.size < limit) {
                        isFinished = true
                    }
                }

                override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
                    if (isFinished) {
                        callback.onResult(emptyList())
                        return
                    }
                    val offset = params.startPosition
                    val limit = params.loadSize
                    val result = loadNextPage(
                        offset = offset,
                        limit = limit
                    )
                    callback.onResult(result)
                    if (result.size < limit) {
                        isFinished = true
                    }
                }
            }
        }
    }

    abstract fun loadFirstPage(offset: Int, limit: Int): List<T>

    open fun loadNextPage(offset: Int, limit: Int): List<T> {
        return emptyList()
    }

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
            .setEnablePlaceholders(false)
            .build()
}

