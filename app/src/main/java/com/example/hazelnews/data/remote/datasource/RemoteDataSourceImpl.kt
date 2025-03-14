package com.example.hazelnews.data.remote.datasource

import com.example.hazelnews.data.remote.api.NewsApI
import com.example.hazelnews.domain.models.NewsResponse
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSourceImpl @Inject constructor(
    private val newsApi: NewsApI
) : RemoteDataSource {

    override suspend fun getHeadlines(countryCode: String, page: Int): Response<NewsResponse> {
        return newsApi.getHeadlines(countryCode, page)
    }

    override suspend fun searchNews(query: String, page: Int): Response<NewsResponse> {
        return newsApi.searchForNews(query, page)
    }
}
