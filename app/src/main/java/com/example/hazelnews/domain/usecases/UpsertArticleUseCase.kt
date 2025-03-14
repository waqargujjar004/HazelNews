package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.domain.repository.NewsRepository
import javax.inject.Inject

class UpsertArticleUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(article: Article): Long {
        return newsRepository.upsert(article)
    }
}
