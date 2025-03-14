package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.domain.repository.NewsRepository
import javax.inject.Inject

class DeleteArticleUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(article: Article) {
        newsRepository.deleteArticle(article)
    }
}
