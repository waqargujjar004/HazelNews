package com.example.hazelnews.ui.events
import com.example.hazelnews.domain.models.Article
sealed class NewsEvent {
    data class FetchHeadlines(val countryCode: String) : NewsEvent()
    data class SearchNews(val query: String) : NewsEvent()
    object LoadMoreSearchResults : NewsEvent()
    data class ToggleFavorite(val article: Article) : NewsEvent()
    data class CheckFavoriteStatus(val url: String) : NewsEvent()
    data class DeleteArticle(val article: Article) : NewsEvent()
    object FetchSavedArticles : NewsEvent()
}