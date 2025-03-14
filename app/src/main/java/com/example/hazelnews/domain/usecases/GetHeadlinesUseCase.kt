package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.NewsResponse
import com.example.hazelnews.domain.repository.NewsRepository
import com.example.hazelnews.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject

class GetHeadlinesUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(countryCode: String, pageNumber: Int): Resource<NewsResponse> {
        return try {
            val response = newsRepository.getHeadlines(countryCode, pageNumber)
            handleResponse(response)
        } catch (e: Exception) {
            Resource.Error("Failed to fetch headlines: ${e.message}")
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
