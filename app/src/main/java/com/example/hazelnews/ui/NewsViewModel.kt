package com.example.hazelnews.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.domain.models.NewsResponse
import com.example.hazelnews.domain.usecases.*
//import com.example.hazelnews.domain.util.Resource
import com.example.hazelnews.util.Resource
import com.example.hazelnews.util.Constants
import com.example.hazelnews.ui.events.NewsEvent
import com.example.hazelnews.ui.state.NewsState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getHeadlinesUseCase: GetHeadlinesUseCase,
    private val getFavoriteNewsUseCase: GetFavoriteNewsUseCase,
    private val searchNewsUseCase: SearchNewsUseCase,
    private val saveArticleUseCase: UpsertArticleUseCase,
    private val deleteArticleUseCase: DeleteArticleUseCase,
    // private val getSavedArticlesUseCase: GetSavedArticlesUseCase,
    private val isArticleExistsUseCase: IsArticleExistsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<NewsState>(NewsState.Loading)
    val state: StateFlow<NewsState> = _state

    private var headlinesPage = 1
    private var apiTotalResults = 0
    private var searchNewsPage = 1
    private var newSearchQuery: String? = null
    private var oldSearchQuery: String? = null
    private var headlinesResponse: MutableList<Article> = mutableListOf()
    private var searchNewsResponse: MutableList<Article> = mutableListOf()

    init {
        onEvent(NewsEvent.FetchHeadlines("us"))
        // observeFavoriteArticles()
    }

    fun onEvent(event: NewsEvent) {
        when (event) {
            is NewsEvent.FetchHeadlines -> fetchHeadlines(event.countryCode)
            is NewsEvent.SearchNews -> searchNews(event.query)
            is NewsEvent.ToggleFavorite -> toggleFavorite(event.article)
            is NewsEvent.DeleteArticle -> deleteArticle(event.article)
            is NewsEvent.CheckFavoriteStatus -> checkIfArticleIsFavorite(event.url)
            is NewsEvent.FetchSavedArticles -> fetchSavedArticles()
            is NewsEvent.LoadMoreSearchResults -> loadMoreSearchResults()

        }
    }



    private fun loadMoreSearchResults() {
        if (_state.value is NewsState.Loading) return // Prevent multiple API calls

        viewModelScope.launch {
            _state.value = NewsState.Loading
            try {
                if (internetConnection()) {
                    val nextPage = searchNewsPage + 1  // Increment page number
                    val result = searchNewsUseCase(newSearchQuery ?: "", nextPage)

                    if (result is Resource.Success) {
                        val articles = result.data?.articles ?: emptyList()
                        apiTotalResults = result.data?.totalResults ?: 0

                        // Append new articles to existing search results
                        searchNewsResponse.addAll(articles)
                        searchNewsPage = nextPage  // Update page number

                        _state.value = NewsState.Success(
                            articles = searchNewsResponse,
                            totalResults = apiTotalResults,
                            isLastPage = (searchNewsPage * 20) >= apiTotalResults,
                            isSearch = true
                        )
                    } else {
                        _state.value = NewsState.Error(result.message ?: "Unknown error")
                    }
                } else {
                    _state.value = NewsState.Error("No internet connection")
                }
            } catch (e: Exception) {
                _state.value = NewsState.Error("Unexpected error: ${e.message}")
            }
        }
    }


    private fun fetchSavedArticles() {
        viewModelScope.launch {
            getFavoriteNewsUseCase().collect { articles ->
                Log.d("NewsViewModel", "Fetching saved articles event received")
                _state.value = NewsState.SavedArticlesState(articles)
            // 🔥 Emit new state
            }
        }
    }


    private fun checkIfArticleIsFavorite(url: String) {
        viewModelScope.launch {
            val isFavorite = isArticleExistsUseCase(url)
            _state.value = NewsState.ArticleFavoriteState(isFavorite)
        }
    }


    private fun fetchHeadlines(countryCode: String) {
        viewModelScope.launch {
            _state.value = NewsState.Loading
            try {
                if (internetConnection()) {
                    val result = getHeadlinesUseCase(countryCode, headlinesPage)
                   // headlinesPage++
                    if (result is Resource.Success) {
                        val articles = result.data?.articles ?: emptyList()
                        apiTotalResults = result.data?.totalResults ?: 0
                        headlinesResponse.addAll(articles)
                        headlinesPage++


                        _state.value = NewsState.Success(
                            articles = headlinesResponse,
                            totalResults = apiTotalResults,
                            isLastPage = (headlinesPage * 20) >= apiTotalResults,
                            isSearch = false


                        )

                    } else {
                        _state.value = NewsState.Error(result.message ?: "Unknown error")
                    }
                } else {
                    _state.value = NewsState.Error("No internet connection")
                }
            } catch (e: Exception) {
                _state.value = NewsState.Error("Unexpected error: ${e.message}")
            }
        }
    }
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        if (_searchQuery.value != query) { // 🔥 Prevent duplicate API calls
            _searchQuery.value = query
        }
    }
    private fun searchNews(query: String) {
        Log.d("SearchNews", "Searching for: $query")

        if (query.isEmpty()) return
        viewModelScope.launch {
            if (query != oldSearchQuery || searchNewsResponse.isEmpty()) {
                searchNewsPage = 1  // Reset page number for new query
                searchNewsResponse.clear()  // Clear previous results
                oldSearchQuery = query  // Update query tracker
            }

            _state.value = NewsState.Loading  // Show loading state
            try {
                if (internetConnection()) {
                    val result = searchNewsUseCase(query, searchNewsPage)
                    if (result is Resource.Success) {
                        val articles = result.data?.articles ?: emptyList()
                        apiTotalResults = result.data?.totalResults ?: 0

                        searchNewsResponse.addAll(articles)
                        searchNewsPage++  // Increase page number after successful response

                        _state.value = NewsState.SearchResults(
                            articles = searchNewsResponse,
                            isLastPage = (searchNewsPage * Constants.QUERY_PAGE_SIZE) >= apiTotalResults
                        )
                    } else {
                        _state.value = NewsState.Error(result.message ?: "Unknown error")
                    }
                } else {
                    _state.value = NewsState.Error("No internet connection")
                }
            } catch (e: Exception) {
                _state.value = NewsState.Error("Unexpected error: ${e.message}")
            }
        }
    }


    private fun toggleFavorite(article: Article) {
        viewModelScope.launch {
            article.url?.let { url ->
                val isFavorite = isArticleExistsUseCase(url)
                Log.d("RoomDB", "Checking if article exists: $isFavorite") // ✅ Debug log

                if (!isFavorite) { // 🔥 Only save if not already a favorite
                    saveArticleUseCase(article)
                    Log.d("RoomDB", "Article Added to Favorites") // ✅ Debug log
                } else {
                    Log.d("RoomDB", "Article already in favorites, no action taken.") // ✅ Debug log
                }

                // Recheck if article is saved after operation
                val updatedStatus = isArticleExistsUseCase(url)
                Log.d("RoomDB", "After Save: Is Favorite? $updatedStatus") // ✅ Debug log
            } ?: Log.e("NewsViewModel", "Article URL is null, cannot check favorites")
        }
    }


//    private fun toggleFavorite(article: Article) {
//        viewModelScope.launch {
//            article.url?.let { url ->
//                val isFavorite = isArticleExistsUseCase(url)
//                Log.d("RoomDB", "Checking if article exists: $isFavorite")
//                if (isFavorite) {
//                    deleteArticleUseCase(article)
//                } else {
//                    saveArticleUseCase(article)
//                }
//            } ?: Log.e("NewsViewModel", "Article URL is null, cannot check favorites")
//        }
//    }

    private fun deleteArticle(article: Article) {
        viewModelScope.launch {
            deleteArticleUseCase(article)
        }
    }
    fun getCurrentPage(): Int = headlinesPage

    private fun internetConnection(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }
}














//package com.example.hazelnews.ui
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.hazelnews.domain.models.Article
//import com.example.hazelnews.domain.models.NewsResponse
//import com.example.hazelnews.domain.usecases.*
//import com.example.hazelnews.util.Resource
//import dagger.hilt.android.lifecycle.HiltViewModel
//import dagger.hilt.android.qualifiers.ApplicationContext
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import retrofit2.Response
//import java.io.IOException
//import javax.inject.Inject
//
//@HiltViewModel
//class NewsViewModel @Inject constructor(
//    private val getHeadlinesUseCase: GetHeadlinesUseCase,
//    private val searchNewsUseCase: SearchNewsUseCase,
//    private val saveArticleUseCase: UpsertArticleUseCase,
//    private val deleteArticleUseCase: DeleteArticleUseCase,
//   // private val getSavedArticlesUseCase: GetSavedArticlesUseCase,
//    private val isArticleExistsUseCase: IsArticleExistsUseCase,
//    @ApplicationContext private val context: Context
//) : ViewModel() {
//
//    private val _headlines = MutableStateFlow<Resource<NewsResponse>>(Resource.Loading())
//    val headlines: StateFlow<Resource<NewsResponse>> = _headlines
//    private var headlinesPage = 1
//    private var headlinesResponse: NewsResponse? = null
//
//    private val _searchNews = MutableStateFlow<Resource<NewsResponse>>(Resource.Loading())
//    val searchNews: StateFlow<Resource<NewsResponse>> = _searchNews
//    private var searchNewsPage = 1
//    private var newSearchQuery: String? = null
//    private var oldSearchQuery: String? = null
//    private var searchNewsResponse: NewsResponse? = null
//
//    private val _favoriteArticles = MutableStateFlow<List<Article>>(emptyList())
//    val favoriteArticles: StateFlow<List<Article>> = _favoriteArticles
//
//    init {
//        getHeadlines("us")
//       // observeFavoriteArticles()
//    }
//
////    private fun observeFavoriteArticles() {
////        viewModelScope.launch {
////            getSavedArticlesUseCase().collectLatest { articles ->
////                _favoriteArticles.value = articles
////            }
////        }
////    }
//
//    fun getHeadlines(countryCode: String) = viewModelScope.launch {
//        fetchHeadlines(countryCode)
//    }
//
//    fun searchNews(searchQuery: String) = viewModelScope.launch {
//        fetchSearchNews(searchQuery)
//    }
//
//    fun addToFavourites(article: Article) = viewModelScope.launch {
//        article.url?.let { url ->
//            val isFavorite = isArticleExistsUseCase(url)
//            if (isFavorite) {
//                deleteArticleUseCase(article)
//            } else {
//                saveArticleUseCase(article)
//            }
//        } ?: Log.e("NewsViewModel", "Article URL is null, cannot check favorites")
//    }
//
//    fun deleteArticle(article: Article) = viewModelScope.launch {
//        deleteArticleUseCase(article)
//    }
//
//    private suspend fun fetchHeadlines(countryCode: String) {
//        _headlines.value = Resource.Loading()
//        try {
//            if (internetConnection()) {
//               // val response = getHeadlinesUseCase(countryCode, headlinesPage)
//                _headlines.value =getHeadlinesUseCase(countryCode, headlinesPage)
//            } else {
//                _headlines.value = Resource.Error("No internet connection")
//            }
//        } catch (t: Throwable) {
//            _headlines.value = Resource.Error(handleThrowable(t))
//        }
//    }
//
//    private suspend fun fetchSearchNews(searchQuery: String) {
//        newSearchQuery = searchQuery
//        _searchNews.value = Resource.Loading()
//        try {
//            if (internetConnection()) {
//                //val response = searchNewsUseCase(searchQuery, searchNewsPage)
//                _searchNews.value = searchNewsUseCase(searchQuery, searchNewsPage)
//            } else {
//                _searchNews.value = Resource.Error("No internet connection")
//            }
//        } catch (t: Throwable) {
//            _searchNews.value = Resource.Error(handleThrowable(t))
//        }
//    }
////
////    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
////        if (response.isSuccessful) {
////            response.body()?.let { resultResponse ->
////                headlinesPage++
////                headlinesResponse = headlinesResponse?.apply {
////                    articles.addAll(resultResponse.articles)
////                } ?: resultResponse
////                return Resource.Success(headlinesResponse!!)
////            }
////        }
////        return Resource.Error(response.message())
////    }
////
////    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
////        if (response.isSuccessful) {
////            response.body()?.let { resultResponse ->
////                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
////                    searchNewsPage = 1
////                    oldSearchQuery = newSearchQuery
////                    searchNewsResponse = resultResponse
////                } else {
////                    searchNewsPage++
////                    searchNewsResponse?.articles?.addAll(resultResponse.articles)
////                }
////                return Resource.Success(searchNewsResponse!!)
////            }
////        }
////        return Resource.Error(response.message())
////    }
//
//    private fun internetConnection(): Boolean {
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
//            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
//                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
//                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
//        } ?: false
//    }
//
//    private fun handleThrowable(t: Throwable): String {
//        return when (t) {
//            is IOException -> "Network failure"
//            else -> "Unexpected error"
//        }
//    }
//}
//
//
//














//package com.example.hazelnews.ui
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.hazelnews.domain.models.Article
//import com.example.hazelnews.domain.models.NewsResponse
//import com.example.hazelnews.domain.repository.NewsRepository
//import com.example.hazelnews.util.Resource
//import dagger.hilt.android.lifecycle.HiltViewModel
//import dagger.hilt.android.qualifiers.ApplicationContext
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import retrofit2.Response
//import java.io.IOException
//import javax.inject.Inject
//
//@HiltViewModel
//class NewsViewModel @Inject constructor(
//    private val newsRepository: NewsRepository,
//    @ApplicationContext private val context: Context
//) : ViewModel() {
//
//    private val _headlines = MutableStateFlow<Resource<NewsResponse>>(Resource.Loading())
//    val headlines: StateFlow<Resource<NewsResponse>> = _headlines
//    private var headlinesPage = 1
//    private var headlinesResponse: NewsResponse? = null
//
//    private val _searchNews = MutableStateFlow<Resource<NewsResponse>>(Resource.Loading())
//    val searchNews: StateFlow<Resource<NewsResponse>> = _searchNews
//    private var searchNewsPage = 1
//    private var newSearchQuery: String? = null
//    private var oldSearchQuery: String? = null
//    private var searchNewsResponse: NewsResponse? = null
//
//    private val _favoriteArticles = MutableStateFlow<List<Article>>(emptyList())
//    val favoriteArticles: StateFlow<List<Article>> = _favoriteArticles
//
//    init {
//        getHeadlines("us")
//        observeFavoriteArticles()
//    }
//
//    private fun observeFavoriteArticles() {
//        viewModelScope.launch {
//            newsRepository.getFavouriteNews().collectLatest { articles ->
//                _favoriteArticles.value = articles
//            }
//        }
//    }
//
//    fun getHeadlines(countryCode: String) = viewModelScope.launch {
//        fetchHeadlines(countryCode)
//    }
//
//    fun searchNews(searchQuery: String) = viewModelScope.launch {
//        fetchSearchNews(searchQuery)
//    }
//
//    fun addToFavourites(article: Article) = viewModelScope.launch {
//        article.url?.let { url ->
//            val isFavorite = newsRepository.isArticleExists(url)
//            if (isFavorite) {
//                newsRepository.deleteArticle(article)
//            } else {
//                newsRepository.upsert(article)
//            }
//        } ?: Log.e("NewsViewModel", "Article URL is null, cannot check favorites")
//    }
//
//    fun deleteArticle(article: Article) = viewModelScope.launch {
//        newsRepository.deleteArticle(article)
//    }
//
//    private suspend fun fetchHeadlines(countryCode: String) {
//        _headlines.value = Resource.Loading()
//        try {
//            if (internetConnection()) {
//                val response = newsRepository.getHeadlines(countryCode, headlinesPage)
//                _headlines.value = handleHeadlinesResponse(response)
//            } else {
//                _headlines.value = Resource.Error("No internet connection")
//            }
//        } catch (t: Throwable) {
//            _headlines.value = Resource.Error(handleThrowable(t))
//        }
//    }
//
//    private suspend fun fetchSearchNews(searchQuery: String) {
//        newSearchQuery = searchQuery
//        _searchNews.value = Resource.Loading()
//        try {
//            if (internetConnection()) {
//                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
//                _searchNews.value = handleSearchNewsResponse(response)
//            } else {
//                _searchNews.value = Resource.Error("No internet connection")
//            }
//        } catch (t: Throwable) {
//            _searchNews.value = Resource.Error(handleThrowable(t))
//        }
//    }
//
//    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
//        if (response.isSuccessful) {
//            response.body()?.let { resultResponse ->
//                headlinesPage++
//                headlinesResponse = headlinesResponse?.apply {
//                    articles.addAll(resultResponse.articles)
//                } ?: resultResponse
//                return Resource.Success(headlinesResponse!!)
//            }
//        }
//        return Resource.Error(response.message())
//    }
//
//    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
//        if (response.isSuccessful) {
//            response.body()?.let { resultResponse ->
//                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
//                    searchNewsPage = 1
//                    oldSearchQuery = newSearchQuery
//                    searchNewsResponse = resultResponse
//                } else {
//                    searchNewsPage++
//                    searchNewsResponse?.articles?.addAll(resultResponse.articles)
//                }
//                return Resource.Success(searchNewsResponse!!)
//            }
//        }
//        return Resource.Error(response.message())
//    }
//
//    private fun internetConnection(): Boolean {
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
//            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
//                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
//                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
//        } ?: false
//    }
//
//    private fun handleThrowable(t: Throwable): String {
//        return when (t) {
//            is IOException -> "Network failure"
//            else -> "Unexpected error"
//        }
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////package com.example.hazelnews.ui
////
////import android.app.Application
////import android.content.Context
////import android.net.ConnectivityManager
////import android.net.NetworkCapabilities
////import android.util.Log
////import androidx.lifecycle.AndroidViewModel
////import androidx.lifecycle.LiveData
////import androidx.lifecycle.MutableLiveData
////import androidx.lifecycle.viewModelScope
////import com.example.hazelnews.domain.models.Article
////import com.example.hazelnews.domain.models.NewsResponse
////import com.example.hazelnews.data.repository.NewsRepository
////import com.example.hazelnews.util.Resource
////import kotlinx.coroutines.launch
////import retrofit2.Response
////import java.io.IOException
////
////class NewsViewModel (app: Application, val newsRepository: NewsRepository): AndroidViewModel(app)  {
////
////    private val _favoriteArticles = MutableLiveData<List<Article>>()
////    val favoriteArticles: LiveData<List<Article>> = _favoriteArticles
////
////
////
////
////    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
////    var headlinesPage = 1
////    var headlinesResponse: NewsResponse? = null
////
////    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
////    var searchNewsPage = 1
////    var searchNewsResponse: NewsResponse? = null
////    var newSearchQuery: String? = null
////    var oldSearchQuery: String? = null
////
////
////
////    init {
////        getHeadlines("us")
////        //observeFavoriteArticles()
////    }
////
////
////    private fun observeFavoriteArticles() {
////        newsRepository.getFavouriteNews().observeForever { articles ->
////            _favoriteArticles.postValue(articles)
////        }
////    }
////
////    // Use this function to remove an article properly
////    fun removeFavorite(article: Article) = viewModelScope.launch {
////        newsRepository.deleteArticle(article)
////    }
////
////
////
////
////
////    fun getHeadlines(countryCode: String) = viewModelScope.launch {
////        headlinesInternet(countryCode)
////    }
////
////    fun searchNews(searchQuery: String) = viewModelScope.launch {
////        searchNewsInternet(searchQuery)
////    }
////
////   private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
////           if (response.isSuccessful)
////           {
////               response.body()?.let{
////                   resultResponse ->
////                   headlinesPage++
////                   if (headlinesResponse == null){
////                       headlinesResponse = resultResponse
////                    }
////                   else {
////                       val oldArticles = headlinesResponse?.articles
////                       val newArticles = resultResponse.articles
////                       oldArticles?.addAll(newArticles)
////                   }
////                   return Resource.Success(headlinesResponse?: resultResponse)
////               }
////           }
////       return Resource.Error(response.message())
////   }
////
////    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
////        if (response.isSuccessful)
////        {
////            response.body()?.let{
////                    resultResponse ->
////                //headlinesPage++
////                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery ){
////                    searchNewsPage = 1
////                    oldSearchQuery = newSearchQuery
////                    searchNewsResponse = resultResponse
////
////                }
////                else {
////                    searchNewsPage++
////                    val oldArticles = searchNewsResponse?.articles
////                    val newArticles = resultResponse.articles
////                    oldArticles?.addAll(newArticles)
////                }
////                return Resource.Success(searchNewsResponse?: resultResponse)
////            }
////        }
////        return Resource.Error(response.message())
////    }
////
//////    fun addToFavourites(article: Article) = viewModelScope.launch{
//////        //newsRepository.upsert(article)
//////        val isFavorite = newsRepository.isArticleExists(article.url)
//////        if (isFavorite) {
//////            newsRepository.deleteArticle(article)
//////        } else {
//////            newsRepository.upsert(article)
//////        }
//////    }
////
////
////
////    fun isArticleExists(url: String): LiveData<Boolean> {
////        val result = MutableLiveData<Boolean>()
////        viewModelScope.launch {
////            result.postValue(newsRepository.isArticleExists(url))
////        }
////        return result
////    }
////
//////    fun addToFavourites(article: Article) = viewModelScope.launch {
//////        article.url?.let { url ->  // Ensure URL is not null before calling the function
//////            val isFavorite = newsRepository.isArticleExists(url)
//////            if (isFavorite) {
//////                newsRepository.deleteArticle(article)
//////            } else {
//////                newsRepository.upsert(article)
//////            }
//////        } ?: run {
//////            Log.e("NewsViewModel", "Article URL is null, cannot check favorites")
//////        }
//////    }
////
////    fun addToFavourites(article: Article) = viewModelScope.launch {
////        article.url?.let { url ->
////            val isFavorite = newsRepository.isArticleExists(url) // Ensure proper execution
////            if (isFavorite) {
////                newsRepository.deleteArticle(article)  // Remove from favorites
////            } else {
////                newsRepository.upsert(article)  // Add to favorites
////            }
////            _favoriteArticles.postValue(newsRepository.getFavouriteNews().value) // Force LiveData update
////        } ?: Log.e("NewsViewModel", "Article URL is null, cannot check favorites")
////    }
////
////    fun getFavouriteNews() = newsRepository.getFavouriteNews()
////
////    fun deleteArticle(article: Article) = viewModelScope.launch{
////        newsRepository.deleteArticle(article)
////        _favoriteArticles.postValue(newsRepository.getFavouriteNews().value)
////    }
////
////    fun internetConnection(context: Context): Boolean {
////        val connectivityManager =
////            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
////
////        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
////            when {
////                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
////                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
////                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
////                else -> false
////            }
////        } ?: false
////    }
////
////    private suspend fun headlinesInternet(countryCode: String) {
////        headlines.postValue(Resource.Loading())
////        try {
////            if (internetConnection(this.getApplication())) {
////                val response = newsRepository.getHeadlines(countryCode,headlinesPage)
////                headlines.postValue(handleHeadlinesResponse(response))
////            } else {
////                headlines.postValue(Resource.Error(message = "No internet connection"))
////            }
////        } catch (t: Throwable) {
////            when (t) {
////                is IOException -> headlines.postValue(Resource.Error(message = "Unable to connect"))
////                else -> headlines.postValue(Resource.Error(message = "No signal"))
////            }
////        }
////    }
////
////
////    private suspend fun searchNewsInternet(searchQuery: String) {
////        newSearchQuery = searchQuery
////        searchNews.postValue(Resource.Loading())
////        try {
////            if (internetConnection(this.getApplication())) {
////                val response = newsRepository.searchNews(searchQuery,searchNewsPage)
////                searchNews.postValue(handleSearchNewsResponse(response))
////            } else {
////                searchNews.postValue(Resource.Error(message = "No internet connection"))
////            }
////        } catch (t: Throwable) {
////            when (t) {
////                is IOException -> searchNews.postValue(Resource.Error(message =  "Network failure"))
////                else -> searchNews.postValue(Resource.Error(message =  "Conversion error"))
////            }
////        }
////    }
////
////
////
////}