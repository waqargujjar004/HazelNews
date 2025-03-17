package com.example.hazelnews.data.local.datasources

import com.example.hazelnews.data.local.db.ArticleDAO
import com.example.hazelnews.domain.models.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalDataSourceImpl @Inject constructor(
    private val articleDao: ArticleDAO
) : LocalDataSource {

    override suspend fun saveArticle(article: Article): Long {
        return  withContext(Dispatchers.IO){
        articleDao.upsert(article)
            }
    }

    override fun getSavedArticles(): Flow<List<Article>> {
        return articleDao.getAllArticles()

    }

    override suspend fun deleteArticle(article: Article) {
        withContext(Dispatchers.IO) {
            articleDao.deleteArticle(article)
        }
    }

    override suspend fun isArticleExists(url: String): Boolean {
        return withContext(Dispatchers.IO) {
             articleDao.isArticleExists(url)
        }
    }
}
