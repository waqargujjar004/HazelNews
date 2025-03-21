package com.example.hazelnews.domain.usecases

import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.util.Resource
import kotlinx.coroutines.flow.Flow

interface SearchNewsUseCase {
    suspend operator fun invoke(query: String, resetPagination: Boolean = false): Flow<Resource<List<Article>>>
}
