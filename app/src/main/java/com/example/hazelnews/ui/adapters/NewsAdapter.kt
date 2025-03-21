package com.example.hazelnews.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
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














//package com.example.hazelnews.ui.adapters
//
////import BaseAdapter
////import BaseAdapter
//import com.hazelmobile.core.baseModule.baseAdapter.BaseAdapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import com.bumptech.glide.Glide
//import com.example.hazelnews.R
//import com.example.hazelnews.domain.models.Article
//
//class NewsAdapter : BaseAdapter<Article>(ArticleDiffCallback()) {
//
//    inner class ArticleViewHolder(itemView: View) : BaseViewHolder<Article>(itemView) {
//        private val articleImage: ImageView = itemView.findViewById(R.id.articleImage)
//        private val articleSource: TextView = itemView.findViewById(R.id.articleSource)
//        private val articleTitle: TextView = itemView.findViewById(R.id.articleTitle)
//        private val articleDescription: TextView = itemView.findViewById(R.id.articleDescription)
//        private val articleDateTime: TextView = itemView.findViewById(R.id.articleDateTime)
//
//        override fun bind(item: Article) {
//            Glide.with(itemView)
//                .load(item.urlToImage)
//                .error(R.drawable.default_image)
//                .into(articleImage)
//
//            articleSource.text = item.source?.name
//            articleTitle.text = item.title
//            articleDescription.text = item.description
//            articleDateTime.text = item.publishedAt
//
//            itemView.setOnClickListener {
//                invokeClickListener(item)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Article> {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
//        return ArticleViewHolder(view)
//    }
//}
















//
//package com.example.hazelnews.ui.adapters
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.view.menu.MenuView.ItemView
//import androidx.recyclerview.widget.AsyncListDiffer
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.hazelnews.R
//import com.example.hazelnews.domain.models.Article
//
//class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>(){
//
//
// inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
//    lateinit var articleImage: ImageView
//    lateinit var articleSource: TextView
//    lateinit var articleTitle: TextView
//    lateinit var articleDescription: TextView
//    lateinit var articleDateTime: TextView
//
//
//    val differ = AsyncListDiffer(this, ArticleDiffCallback())
//
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
//                  return ArticleViewHolder(
//                      LayoutInflater.from(parent.context).inflate(R.layout.item_news,parent,false)
//                          )
//    }
//
//    override fun getItemCount(): Int {
//       return  differ.currentList.size
//    }
//    private var onItemClickListener : ((Article)-> Unit)? = null
//
//    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
//        val article = differ.currentList[position]
//
//        articleImage = holder.itemView.findViewById(R.id.articleImage)
//        articleSource = holder.itemView.findViewById(R.id.articleSource)
//        articleTitle = holder.itemView.findViewById(R.id.articleTitle)
//        articleDescription = holder.itemView.findViewById(R.id.articleDescription)
//        articleDateTime = holder.itemView.findViewById(R.id.articleDateTime)
//
//        holder.itemView.apply {
//            Glide.with(this).load(article.urlToImage).error(R.drawable.default_image).into(articleImage)
//
//            articleSource.text = article.source?.name
//            articleTitle.text = article.title
//            articleDescription.text = article.description
//            articleDateTime.text = article.publishedAt
//
//            setOnClickListener {
//                onItemClickListener?.let {
//                    it(article)
//                }
//            }
//
//        }
//    }
//    fun setOnItemClickListener(listener: (Article) -> Unit){
//        onItemClickListener = listener
//    }
//}