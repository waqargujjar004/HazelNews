package com.example.hazelnews.domain.repository

import androidx.lifecycle.LiveData
import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.domain.models.NewsResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface NewsRepository {
    suspend fun getHeadlines(countryCode: String, pageNumber: Int): Response<NewsResponse>
    suspend fun searchNews(searchQuery: String, pageNumber: Int): Response<NewsResponse>
    suspend fun upsert(article: Article): Long
    fun getFavouriteNews(): Flow<List<Article>>
    suspend fun deleteArticle(article: Article)
    suspend fun isArticleExists(url: String): Boolean
}
