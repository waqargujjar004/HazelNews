package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.NewsResponse
import com.example.hazelnews.domain.repository.NewsRepository
import com.example.hazelnews.util.Resource
import retrofit2.Response
import javax.inject.Inject

class SearchNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(query: String, pageNumber: Int): Resource<NewsResponse> {
        return try {
            val response = newsRepository.searchNews(query, pageNumber)
            handleResponse(response)
        } catch (e: Exception) {
            Resource.Error("Failed to search news: ${e.message}")
        }
    }

    private fun handleResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Resource.Success(it)
            } ?: Resource.Error("Empty response")
        } else {
            Resource.Error("Error: ${response.message()}")
        }
    }
}
