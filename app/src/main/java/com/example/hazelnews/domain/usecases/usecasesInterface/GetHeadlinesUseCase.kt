package com.example.hazelnews.domain.usecases.usecasesInterface
import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.util.Resource
import kotlinx.coroutines.flow.Flow
interface GetHeadlinesUseCase {
    suspend operator fun invoke(countryCode: String, resetPagination: Boolean = false): Flow<Resource<List<Article>>>
}