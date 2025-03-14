package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.repository.NewsRepository
import javax.inject.Inject

class IsArticleExistsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(url: String): Boolean {
        return newsRepository.isArticleExists(url)
    }
}
