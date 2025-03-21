package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.Article

interface UpsertArticleUseCase {
    suspend operator fun invoke(article: Article): Long
}