package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.Article
import kotlinx.coroutines.flow.Flow

interface GetFavoriteNewsUseCase {
    operator fun invoke(): Flow<List<Article>>
}