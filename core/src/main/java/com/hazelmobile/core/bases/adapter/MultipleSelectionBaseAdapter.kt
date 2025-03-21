package com.hazelmobile.cores.bases.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hazelmobile.core.bases.adapter.BaseAdapter
import com.hazelmobile.cores.bases.adapter.helpers.FilterManager
import com.hazelmobile.cores.utils.GenericCallback

abstract class MultipleSelectionBaseAdapter<Model : Any, Binding : ViewBinding>(
    bindingFactory: (LayoutInflater, ViewGroup?, Boolean) -> Binding,
    filterManager: FilterManager<Model>? = null,
    callback: DiffUtil.ItemCallback<Model> = BaseDiffUtils()) : BaseAdapter<Model, Binding>(bindingFactory, callback, filterManager) {

    private val selectedItems = mutableSetOf<Model>()

    // Get the currently selected items
    fun getSelectedItems(): List<Model> = selectedItems.toList()

    // Check if an item is selected
    private fun isSelected(item: Model): Boolean = selectedItems.contains(item)

    //A boolean signal that either all items are selected/ not selected
    private var onSelectionStateChanged: GenericCallback<Boolean>? = null

    fun onSelectionStateChanged(callback: GenericCallback<Boolean>) {
        this.onSelectionStateChanged = callback
    }



    fun selectAllItemsAction(selection: Selection){
        when(selection){
            is Selection.SelectAll->{
                selectAll()
            }
            is Selection.UnSelectAll->{
                unselectAll()
            }

        }
    }
    // Select all items
    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(currentList)
        notifyAllSelectionState()
    }

    // Unselect all items
    fun unselectAll() {
        selectedItems.clear()
        notifyAllSelectionState()
    }

    // Notify selection state for all items
    private fun notifyAllSelectionState() {
        currentList.forEachIndexed { index, _ ->
            notifyItemChanged(index)
        }
        onSelectionStateChanged?.invoke(selectedItems.size == currentList.size)
    }



    // Handle item selection
    private fun toggleSelection(holder: BaseViewHolder<Binding>, item: Model) {
        val position = holder.absoluteAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }
            notifyItemChanged(position)
            onSelectionStateChanged?.invoke(selectedItems.size == currentList.size)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Binding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        with(holder) {
            binding.bindSelectionState(item, isSelected(item))
        }
        holder.binding.root.setOnClickListener {
            toggleSelection(holder, item)
            onItemCallback?.invoke(item)
        }
    }

    // Abstract function for binding selection state (to be implemented in concrete adapter)
    abstract fun Binding.bindSelectionState(item: Model, isSelected: Boolean)

}


