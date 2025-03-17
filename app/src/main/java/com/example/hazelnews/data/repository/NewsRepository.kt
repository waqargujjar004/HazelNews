package com.example.hazelnews.data.repository

import com.example.hazelnews.data.local.datasources.LocalDataSource
import com.example.hazelnews.data.remote.datasource.RemoteDataSource
import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.domain.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : NewsRepository {

    override suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
        withContext(Dispatchers.IO) {
            remoteDataSource.getHeadlines(countryCode, pageNumber)
        }

    override suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        withContext(Dispatchers.IO) {
            remoteDataSource.searchNews(searchQuery, pageNumber)
        }

    override suspend fun upsert(article: Article) =
        withContext(Dispatchers.IO) {
            localDataSource.saveArticle(article)
        }

    override fun getFavouriteNews() =
        localDataSource.getSavedArticles()

    override suspend fun deleteArticle(article: Article) =
        withContext(Dispatchers.IO) {
            localDataSource.deleteArticle(article)
        }

    override suspend fun isArticleExists(url: String): Boolean =
        withContext(Dispatchers.IO) {
            localDataSource.isArticleExists(url)
        }
}
































//package com.example.hazelnews.data.repository
//
//import com.example.hazelnews.data.remote.api.RetrofitInstance
//import com.example.hazelnews.data.local.db.ArticleDatabase
//import com.example.hazelnews.domain.models.Article
//
//class NewsRepository(val db: ArticleDatabase) {
//
//    suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
//        RetrofitInstance.api.getHeadlines(countryCode,pageNumber)
//
//
//    suspend fun searchNews(searchQuery: String,pageNumber: Int) =
//        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)
//
//    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)
//
//    fun getFavouriteNews() = db.getArticleDao().getAllArticles()
//
//    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)
//
//
//    suspend fun isArticleExists(url: String): Boolean {
//        return db.getArticleDao().isArticleExists(url)
//    }
//}