package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.domain.models.NewsResponse
import com.example.hazelnews.domain.repository.NewsRepository
import com.example.hazelnews.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import javax.inject.Inject

class GetHeadlinesUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    private var currentPage = 1
    private var totalResults = 0
    private val headlinesList = mutableListOf<Article>()

    suspend operator fun invoke(countryCode: String, resetPagination: Boolean = false): Flow<Resource<List<Article>>> = flow {
        try {
            if (resetPagination) {
                currentPage = 1
                totalResults = 0
                headlinesList.clear()
            }

            emit(Resource.Loading())

            val response = newsRepository.getHeadlines(countryCode, currentPage)
            val result = handleResponse(response)

            if (result is Resource.Success) {
                val articles = result.data?.articles ?: emptyList()
                totalResults = result.data?.totalResults ?: 0
                headlinesList.addAll(articles)

                currentPage++ // Move to next page

                emit(Resource.Success(headlinesList.toList(), isLastPage = isLastPage()))
            } else {
                emit(Resource.Error(result.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Failed to fetch headlines: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    private fun handleResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Resource.Success(it)
            } ?: Resource.Error("Empty response")
        } else {
            Resource.Error("Error: ${response.message()}")
        }
    }

    private fun isLastPage(): Boolean {
        return (currentPage * 20) >= totalResults
    }
}













//package com.example.hazelnews.domain.usecases
//
//import com.example.hazelnews.domain.models.NewsResponse
//import com.example.hazelnews.domain.repository.NewsRepository
//import com.example.hazelnews.util.Resource
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import retrofit2.Response
//import javax.inject.Inject
//
//class GetHeadlinesUseCase @Inject constructor(
//    private val newsRepository: NewsRepository
//) {
//    suspend operator fun invoke(countryCode: String, pageNumber: Int): Resource<NewsResponse> {
//        return try {
//            val response = newsRepository.getHeadlines(countryCode, pageNumber)
//            handleResponse(response)
//        } catch (e: Exception) {
//            Resource.Error("Failed to fetch headlines: ${e.message}")
//        }
//    }
//
//    private fun handleResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
//        return if (response.isSuccessful) {
//            response.body()?.let {
//                Resource.Success(it)
//            } ?: Resource.Error("Empty response")
//        } else {
//            Resource.Error("Error: ${response.message()}")
//        }
//    }
//}
