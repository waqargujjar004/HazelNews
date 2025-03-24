package com.example.hazelnews.util

import androidx.lifecycle.LifecycleCoroutineScope
import com.example.hazelnews.ui.events.NewsEvent
import com.example.hazelnews.ui.viewmodel.NewsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchQueryHandler(
    private val newsViewModel: NewsViewModel,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    private var searchJob: Job? = null

    fun handleSearch(query: String?) {
        query?.trim()?.let {
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(500)  // Avoid multiple API calls
                if (it.isNotEmpty()) {
                    newsViewModel.onEvent(NewsEvent.SearchNews(it))
                }
            }
        }
    }
}
