package com.example.hazelnews.ui.viewmodel

import android.content.Context
import android.util.Log
import com.hazelmobile.cores.bases.viewmodel.BaseViewModel
import androidx.lifecycle.viewModelScope
import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.domain.usecases.*
import com.example.hazelnews.domain.usecases.usecasesInterface.GetHeadlinesUseCase
import com.example.hazelnews.util.Resource
import com.example.hazelnews.ui.events.NewsEvent
import com.example.hazelnews.ui.state.NewsState
import com.example.hazelnews.util.NetworkHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getHeadlinesUseCase: GetHeadlinesUseCase,
    private val getFavoriteNewsUseCase: GetFavoriteNewsUseCase,
    private val searchNewsUseCase: SearchNewsUseCase,
    private val saveArticleUseCase: UpsertArticleUseCase,
    private val deleteArticleUseCase: DeleteArticleUseCase,
    private val isArticleExistsUseCase: IsArticleExistsUseCase,
    private val networkHelper: NetworkHelper,  //  Moved inside ViewModel
    @ApplicationContext private val context: Context
) : BaseViewModel<NewsEvent, NewsState>() {

    private val _state = MutableStateFlow<NewsState>(NewsState.Loading)
    val state: StateFlow<NewsState> = _state

    private var headlinesPage = 1
    private var apiTotalResults = 0
    private var searchNewsPage = 1
    private var newSearchQuery: String? = null
    private var headlinesResponse: MutableList<Article> = mutableListOf()


    init {
        onEvent(NewsEvent.FetchHeadlines("us"))
    }

    override fun onEvent(action: NewsEvent) {
        when (action) {
            is NewsEvent.FetchHeadlines -> fetchHeadlines(action.countryCode)
            is NewsEvent.SearchNews -> searchNews(action.query)
            is NewsEvent.ToggleFavorite -> toggleFavorite(action.article)
            is NewsEvent.DeleteArticle -> deleteArticle(action.article)
            is NewsEvent.CheckFavoriteStatus -> checkIfArticleIsFavorite(action.url)
            is NewsEvent.FetchSavedArticles -> fetchSavedArticles()
            is NewsEvent.LoadMoreSearchResults -> loadMoreSearchResults()
        }
    }

    private fun fetchHeadlines(countryCode: String) {
        _state.value = NewsState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (networkHelper.hasInternetConnection()) {  // ✅ Proper network check
                    getHeadlinesUseCase(countryCode).collect { result ->
                        withContext(Dispatchers.Main) {
                            if (result is Resource.Success) {
                                val articles = result.data ?: emptyList()
                                apiTotalResults = articles.size
                                headlinesResponse.clear()//Clear old data before adding new
                                headlinesResponse.addAll(articles)

                                withContext(Dispatchers.Main) {
                                    _state.value = NewsState.Success(
                                        articles = headlinesResponse,
                                        totalResults = apiTotalResults,
                                        isLastPage = (headlinesResponse.size >= apiTotalResults),
                                        isSearch = false
                                    )
                                }
                            } else {
                                // _state.value = NewsState.Error(result.message ?: "Unknown error")
                            }
                        }
                    }
                } else {
                    _state.value = NewsState.Error("No internet connection")
                }
            } catch (e: Exception) {
                _state.value = NewsState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    private fun searchNews(query: String) {
        if (query.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = NewsState.Loading
            try {
                if (networkHelper.hasInternetConnection()) {
                    searchNewsUseCase(query, resetPagination = true).collect { result ->
                        withContext(Dispatchers.Main) {
                            if (result is Resource.Success) {
                                _state.value = NewsState.SearchResults(
                                    articles = result.data ?: emptyList(),
                                    isLastPage = result.isLastPage
                                )
                            } else {
                                // _state.value = NewsState.Error(result.message ?: "Unknown error")
                            }
                        }
                    }
                } else {
                    _state.value = NewsState.Error("No internet connection")
                }
            } catch (e: Exception) {
                _state.value = NewsState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    private fun loadMoreSearchResults() {
        if (_state.value is NewsState.Loading) return  // Prevent multiple API calls
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (networkHelper.hasInternetConnection()) {
                    searchNewsUseCase(newSearchQuery ?: "").collect { result ->
                        withContext(Dispatchers.Main) {
                            if (result is Resource.Success) {
                                searchNewsPage++
                                _state.value = NewsState.SearchResults(
                                    articles = result.data ?: emptyList(),
                                    isLastPage = result.isLastPage
                                )
                            } else {
                                // _state.value = NewsState.Error(result.message ?: "Unknown error")
                            }
                        }
                    }
                } else {
                    _state.value = NewsState.Error("No internet connection")
                }
            } catch (e: Exception) {
                _state.value = NewsState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    //private val _savedArticles = MutableStateFlow<List<Article>>(emptyList())
    /*val savedArticles: StateFlow<List<Article>> = _savedArticles.asStateFlow()*/


    private fun fetchSavedArticles() {
        viewModelScope.launch {
            getFavoriteNewsUseCase()
                .stateIn(viewModelScope) // ✅ Ensures collection stops when ViewModel is cleared
                .collectLatest { articles ->
                    Log.d("NewsViewModel", "Fetched articles: $articles") // ✅ Debugging
                  //  _savedArticles.value = articles
                    _state.value = NewsState.SavedArticlesState(articles)
                }
        }
    }


    private fun checkIfArticleIsFavorite(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFavorite = isArticleExistsUseCase(url)
            withContext(Dispatchers.Main) {
                _state.value = NewsState.ArticleFavoriteState(isFavorite)
            }
        }
    }

    private fun toggleFavorite(article: Article) {
        viewModelScope.launch(Dispatchers.IO) {
            article.url?.let { url ->
                val isFavorite = isArticleExistsUseCase(url)
                if (!isFavorite) {
                    saveArticleUseCase(article)
                }
                //val updatedStatus = isArticleExistsUseCase(url)
            }
        }
    }

    private fun deleteArticle(article: Article) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteArticleUseCase(article)
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

//    fun setSearchQuery(query: String) {
//        if (_searchQuery.value != query) {
//            _searchQuery.value = query
//        }
//    }

    fun getCurrentPage(): Int = headlinesPage
}

