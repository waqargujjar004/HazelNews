package com.example.hazelnews.domain.usecases

interface IsArticleExistsUseCase {
    suspend operator fun invoke(url: String): Boolean
}