package com.example.hazelnews.ui.adapters

import com.bumptech.glide.Glide
import com.example.hazelnews.R
import com.example.hazelnews.databinding.ItemNewsBinding
import com.example.hazelnews.domain.models.Article
import com.hazelmobile.core.bases.adapter.BaseAdapter

class NewsAdapter :
    BaseAdapter<Article, ItemNewsBinding>(ItemNewsBinding::inflate, ArticleDiffCallback()) {

    override fun ItemNewsBinding.bindViews(model: Article) {
        // Load article image using Glide
        Glide.with(root.context)
            .load(model.urlToImage)
            .error(R.drawable.default_image)
            .into(articleImage)

        // Set article data
        articleSource.text = model.source?.name
        articleTitle.text = model.title
        articleDescription.text = model.description
        articleDateTime.text = model.publishedAt
    }

    override fun ItemNewsBinding.bindListeners(item: Article) {
        root.setOnClickListener {
            onItemCallback?.invoke(item)
        }
    }
}











