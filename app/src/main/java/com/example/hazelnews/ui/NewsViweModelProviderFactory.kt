//package com.example.hazelnews.ui
//
//import android.app.Application
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.hazelnews.data.repository.NewsRepository
//
//class NewsViweModelProviderFactory (val app : Application, val newsRepository : NewsRepository) :ViewModelProvider.Factory {
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return NewsViewModel(app,newsRepository) as T
//    }
//
//}
