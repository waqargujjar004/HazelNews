package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(): Flow<List<Article>> {
        return newsRepository.getFavouriteNews()
    }
}
