package com.example.hazelnews.domain.usecases.usecasesImpl

import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.data.repository.NewsRepository
import com.example.hazelnews.domain.usecases.DeleteArticleUseCase
import javax.inject.Inject

class DeleteArticleUseCaseImpl @Inject constructor(
    private val newsRepository: NewsRepository
) : DeleteArticleUseCase {
    override suspend operator fun invoke(article: Article) {
        newsRepository.deleteArticle(article)
    }
}
