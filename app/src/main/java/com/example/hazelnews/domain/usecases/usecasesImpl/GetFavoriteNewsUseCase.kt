package com.example.hazelnews.domain.usecases.usecasesImpl

import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.data.repository.NewsRepository
import com.example.hazelnews.domain.usecases.GetFavoriteNewsUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteNewsUseCaseImpl @Inject constructor(
    private val newsRepository: NewsRepository
) : GetFavoriteNewsUseCase {
    override operator fun invoke(): Flow<List<Article>> {
        return newsRepository.getFavouriteNews()
    }
}
