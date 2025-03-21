package com.hazelmobile.cores.bases.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding

abstract class MultipleViewBaseAdapter<Model : Any, Binding : ViewBinding>(
    private val bindingFactories: Map<Int, (LayoutInflater, ViewGroup?, Boolean) -> Binding>,
    callback: DiffUtil.ItemCallback<Model> = BaseDiffUtils(),
) : ListAdapter<Model, BaseViewHolder<Binding>>(callback) {

    override fun getItemViewType(position: Int): Int {
        return getViewType(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Binding> {
        val bindingFactory = bindingFactories[viewType]
            ?: throw IllegalArgumentException("No binding factory found for view type: $viewType")
        return BaseViewHolder(bindingFactory.invoke(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Binding>, position: Int) {
        with(holder) {
            val item = getItem(absoluteAdapterPosition)
            binding.root.tag = absoluteAdapterPosition
            binding.bindViews(item)
            binding.bindListeners(absoluteAdapterPosition)
            binding.bindListeners(getItem(absoluteAdapterPosition))
        }
    }

    override fun submitList(list: List<Model>?) {
        super.submitList(list?.toMutableList())
    }

    protected abstract fun getViewType(item: Model): Int

    protected abstract fun ViewBinding.bindViews(model: Model)

    open fun Binding.bindListeners(position: Int) {}
    open fun Binding.bindListeners(item: Model) {}

}


