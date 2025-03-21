package com.hazelmobile.cores.bases.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class BaseViewHolder<Binding : ViewBinding>(val binding: Binding) : RecyclerView.ViewHolder(binding.root)
