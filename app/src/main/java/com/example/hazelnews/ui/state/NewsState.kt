package com.example.hazelnews.ui.state

import com.example.hazelnews.domain.models.Article

sealed class NewsState {
    object Loading : NewsState()
    data class Success(
        val articles: List<Article>,
        val totalResults: Int,
        val isSearch: Boolean,
        val isLastPage: Boolean
         // ✅ Add totalResults to track total available articles
    ) : NewsState()
    data class Error(val message: String) : NewsState()
    data class SearchResults(val articles: List<Article>, val isLastPage: Boolean) : NewsState()
    data class ArticleFavoriteState(val isFavorite: Boolean) : NewsState()
    data class SavedArticlesState(val articles: List<Article>) : NewsState()
}
