package com.example.hazelnews.data.local.datasources
import com.example.hazelnews.domain.models.Article
import kotlinx.coroutines.flow.Flow


interface LocalDataSource {

    suspend fun saveArticle(article: Article): Long
    fun getSavedArticles(): Flow<List<Article>>
    suspend fun deleteArticle(article: Article)
    suspend fun isArticleExists(url: String): Boolean
}