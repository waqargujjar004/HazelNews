package com.example.hazelnews.repository

import com.example.hazelnews.api.RetrofitInstance
import com.example.hazelnews.db.ArticleDatabase
import com.example.hazelnews.models.Article

class NewsRepository(val db: ArticleDatabase) {

    suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getHeadlines(countryCode,pageNumber)


    suspend fun searchNews(searchQuery: String,pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    fun getFavouriteNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)


    suspend fun isArticleExists(url: String): Boolean {
        return db.getArticleDao().isArticleExists(url)
    }
}