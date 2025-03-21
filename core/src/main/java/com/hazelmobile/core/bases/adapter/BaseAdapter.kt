package com.hazelmobile.core.bases.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import com.hazelmobile.cores.bases.adapter.BaseDiffUtils
import com.hazelmobile.cores.bases.adapter.BaseViewHolder
import com.hazelmobile.cores.bases.adapter.helpers.FilterManager
import com.hazelmobile.cores.utils.GenericCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseAdapter<Model: Any, Binding : ViewBinding>(
    private val bindingFactory: (LayoutInflater, ViewGroup?, Boolean) -> Binding,
    callback: DiffUtil.ItemCallback<Model> = BaseDiffUtils(),
    private val filterManager: FilterManager<Model>?= null,

    ) : ListAdapter<Model, BaseViewHolder<Binding>>(callback) {

    //Callbacks
    var onItemCallback: GenericCallback<Model>? = null
    var onItemPositionCallback: GenericCallback<Int>? = null
    var onNoResultsFoundCallback: GenericCallback<Boolean>? = null
    private var job: Job?= null

    //Lists
    private val originalList = mutableListOf<Model>()
    private var previousQuery = ""



    fun onItemClickListener(callback: GenericCallback<Model>) {
        this.onItemCallback = callback
    }
    fun onItemPositionClickListener(callback: GenericCallback<Int>) {
        this.onItemPositionCallback = callback
    }

    fun onNoResultsFoundCallback(callback: GenericCallback<Boolean>) {
        this.onNoResultsFoundCallback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : BaseViewHolder<Binding> {
        return BaseViewHolder(bindingFactory.invoke(LayoutInflater.from(parent.context),parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Binding>, position: Int) {
        with(holder) {
            binding.root.tag = absoluteAdapterPosition
            binding.bindViews(getItem(absoluteAdapterPosition))
            binding.bindListeners(absoluteAdapterPosition)
            binding.bindListeners(getItem(absoluteAdapterPosition))
        }
    }

override fun getItemCount(): Int = super.getItemCount()


    fun submitOriginalList(list: List<Model>) {
        originalList.clear()
        originalList.addAll(list)
        submitListAsync(list)
        submitList(list)
    }

    override fun submitList(list: List<Model>?) {
        super.submitList(list?.toMutableList())
    }


private fun submitListAsync(list: List<Model>) {
    CoroutineScope(Dispatchers.Default).launch {
        val newFilteredList = list.toList() // Immutable copy for safety
        withContext(Dispatchers.Main) {
            submitList(newFilteredList) // ListAdapter will handle the DiffUtil internally
        }
    }
}
    fun filter(query: String) {
        job?.cancel()
        job=CoroutineScope(Dispatchers.Default).launch {
            val filteredList = filterManager?.filter(query, originalList, currentList, previousQuery)
            previousQuery = query
            withContext(Dispatchers.Main) {
                submitList(filteredList)
                onNoResultsFoundCallback?.invoke(filteredList?.isEmpty())
            }
        }
    }


    protected abstract fun Binding.bindViews(model: Model)
    open fun Binding.bindListeners(position: Int) {}
    open fun Binding.bindListeners(item: Model) {}

}

