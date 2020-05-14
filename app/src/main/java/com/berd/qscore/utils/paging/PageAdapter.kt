package com.berd.qscore.utils.paging

import androidx.recyclerview.widget.*

data class DataLoader<T>(
    val prefetchDistance: Int = 30,
    val loadInitialCallback: () -> List<T>,
    val loadNextPageCallback: () -> List<T>
)

@Suppress("LeakingThis")
abstract class PageAdapter<T, VH : RecyclerView.ViewHolder?>(
    private val dataLoader: DataLoader<T>
) : RecyclerView.Adapter<VH>() {

    abstract val diffCallback: DiffUtil.ItemCallback<T>
    private val dataSet = linkedSetOf<T>()

    private val differ: AsyncListDiffer<T> = AsyncListDiffer(
        AdapterListUpdateCallback(this),
        AsyncDifferConfig.Builder(diffCallback).build()
    )

    fun startLoading(dataLoader: DataLoader<T>) {
        val items = dataLoader.loadInitialCallback()
        submitInitialList(items)
    }

    private fun submitInitialList(items: List<T>) {
        dataSet.clear()
        dataSet.addAll(items)
        differ.submitList(dataSet.toList())
    }

    private fun submitNextPage(items: List<T>) {
        dataSet.addAll(items)
        differ.submitList(dataSet.toList())
    }

    override fun onViewAttachedToWindow(holder: VH) {
        val position = holder?.adapterPosition ?: 0
        if (position > itemCount - dataLoader.prefetchDistance) {
            val items = dataLoader.loadNextPageCallback()
            submitNextPage(items)
        }
    }

    protected fun getItem(position: Int): T {
        return differ.currentList[position]
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}
