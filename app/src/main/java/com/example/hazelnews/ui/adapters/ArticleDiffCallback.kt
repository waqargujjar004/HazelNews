package com.example.hazelnews.ui.adapters

import com.hazelmobile.cores.bases.adapter.BaseDiffUtils
import com.example.hazelnews.domain.models.Article

class ArticleDiffCallback : BaseDiffUtils<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.url == newItem.url  // Compare unique identifier
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem  // Compare full object data
    }
}