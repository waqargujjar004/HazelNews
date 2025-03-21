package com.example.hazelnews.domain.usecases.usecasesImpl

import com.example.hazelnews.data.repository.NewsRepository
import com.example.hazelnews.domain.usecases.IsArticleExistsUseCase
import javax.inject.Inject

class IsArticleExistsUseCaseImpl @Inject constructor(
    private val newsRepository: NewsRepository
) : IsArticleExistsUseCase {
    override suspend operator fun invoke(url: String): Boolean {
        return newsRepository.isArticleExists(url)
    }
}
