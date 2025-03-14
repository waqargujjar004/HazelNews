package com.example.hazelnews.data.remote.datasource

import com.example.hazelnews.domain.models.NewsResponse
import retrofit2.Response

interface RemoteDataSource {
    suspend fun getHeadlines(countryCode: String, page: Int): Response<NewsResponse>
    suspend fun searchNews(query: String, page: Int): Response<NewsResponse>
}