package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.Article

interface DeleteArticleUseCase {
    suspend operator fun invoke(article: Article)
}