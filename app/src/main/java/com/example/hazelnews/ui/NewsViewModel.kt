package com.example.hazelnews.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hazelnews.models.Article
import com.example.hazelnews.models.NewsResponse
import com.example.hazelnews.repository.NewsRepository
import com.example.hazelnews.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel (app: Application, val newsRepository: NewsRepository): AndroidViewModel(app)  {

    private val _favoriteArticles = MutableLiveData<List<Article>>()
    val favoriteArticles: LiveData<List<Article>> = _favoriteArticles




    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinesPage = 1
    var headlinesResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null



    init {
        getHeadlines("us")
        //observeFavoriteArticles()
    }


    private fun observeFavoriteArticles() {
        newsRepository.getFavouriteNews().observeForever { articles ->
            _favoriteArticles.postValue(articles)
        }
    }

    // Use this function to remove an article properly
    fun removeFavorite(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }





    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

   private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
           if (response.isSuccessful)
           {
               response.body()?.let{
                   resultResponse ->
                   headlinesPage++
                   if (headlinesResponse == null){
                       headlinesResponse = resultResponse
                    }
                   else {
                       val oldArticles = headlinesResponse?.articles
                       val newArticles = resultResponse.articles
                       oldArticles?.addAll(newArticles)
                   }
                   return Resource.Success(headlinesResponse?: resultResponse)
               }
           }
       return Resource.Error(response.message())
   }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful)
        {
            response.body()?.let{
                    resultResponse ->
                //headlinesPage++
                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery ){
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse

                }
                else {
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

//    fun addToFavourites(article: Article) = viewModelScope.launch{
//        //newsRepository.upsert(article)
//        val isFavorite = newsRepository.isArticleExists(article.url)
//        if (isFavorite) {
//            newsRepository.deleteArticle(article)
//        } else {
//            newsRepository.upsert(article)
//        }
//    }



    fun isArticleExists(url: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            result.postValue(newsRepository.isArticleExists(url))
        }
        return result
    }

//    fun addToFavourites(article: Article) = viewModelScope.launch {
//        article.url?.let { url ->  // Ensure URL is not null before calling the function
//            val isFavorite = newsRepository.isArticleExists(url)
//            if (isFavorite) {
//                newsRepository.deleteArticle(article)
//            } else {
//                newsRepository.upsert(article)
//            }
//        } ?: run {
//            Log.e("NewsViewModel", "Article URL is null, cannot check favorites")
//        }
//    }

    fun addToFavourites(article: Article) = viewModelScope.launch {
        article.url?.let { url ->
            val isFavorite = newsRepository.isArticleExists(url) // Ensure proper execution
            if (isFavorite) {
                newsRepository.deleteArticle(article)  // Remove from favorites
            } else {
                newsRepository.upsert(article)  // Add to favorites
            }
            _favoriteArticles.postValue(newsRepository.getFavouriteNews().value) // Force LiveData update
        } ?: Log.e("NewsViewModel", "Article URL is null, cannot check favorites")
    }

    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch{
        newsRepository.deleteArticle(article)
        _favoriteArticles.postValue(newsRepository.getFavouriteNews().value)
    }

    fun internetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
            when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } ?: false
    }

    private suspend fun headlinesInternet(countryCode: String) {
        headlines.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())) {
                val response = newsRepository.getHeadlines(countryCode,headlinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error(message = "No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resource.Error(message = "Unable to connect"))
                else -> headlines.postValue(Resource.Error(message = "No signal"))
            }
        }
    }


    private suspend fun searchNewsInternet(searchQuery: String) {
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())) {
                val response = newsRepository.searchNews(searchQuery,searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error(message = "No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error(message =  "Network failure"))
                else -> searchNews.postValue(Resource.Error(message =  "Conversion error"))
            }
        }
    }



}