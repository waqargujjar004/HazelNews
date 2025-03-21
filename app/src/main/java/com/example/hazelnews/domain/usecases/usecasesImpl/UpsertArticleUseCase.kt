package com.example.hazelnews.domain.usecases.usecasesImpl

import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.data.repository.NewsRepository
import com.example.hazelnews.domain.usecases.UpsertArticleUseCase
import javax.inject.Inject

class UpsertArticleUseCaseImpl @Inject constructor(
    private val newsRepository: NewsRepository
): UpsertArticleUseCase {
    override suspend operator fun invoke(article: Article): Long {
        return newsRepository.upsert(article)
    }
}
