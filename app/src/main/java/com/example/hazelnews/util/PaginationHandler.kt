package com.example.hazelnews.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hazelnews.ui.events.NewsEvent
import com.example.hazelnews.ui.viewmodel.NewsViewModel
class PaginationHandler(
    private val recyclerView: RecyclerView,
    private var isLastPageProvider:Boolean = false, // Function to get isLastPage
    private val onLoadMore: () -> Unit // Callback to trigger loading more data
) {
    private var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val shouldPaginate = !isLastPageProvider && isScrolling &&
                    firstVisibleItemPosition + visibleItemCount >= totalItemCount &&
                    firstVisibleItemPosition >= 0 &&
                    totalItemCount >= Constants.QUERY_PAGE_SIZE

            if (shouldPaginate) {
                onLoadMore() // Call the correct event
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) isScrolling = true
        }
    }

    fun setupScrollListener() {
        recyclerView.addOnScrollListener(scrollListener)
    }
    fun updateLastPageState(isLastPage: Boolean) {
        this.isLastPageProvider = isLastPage
    }
}






//class PaginationHandler(
//    private var newsViewModel: NewsViewModel,
//    private var recyclerView: RecyclerView,
//    private var isLastPage: Boolean,
//    private var isScrolling: Boolean
//) {
//    private val scrollListener = object : RecyclerView.OnScrollListener() {
//        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            super.onScrolled(recyclerView, dx, dy)
//            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//            val visibleItemCount = layoutManager.childCount
//            val totalItemCount = layoutManager.itemCount
//
//            val shouldPaginate = !isLastPage && isScrolling &&
//                    firstVisibleItemPosition + visibleItemCount >= totalItemCount &&
//                    firstVisibleItemPosition >= 0 &&
//                    totalItemCount >= Constants.QUERY_PAGE_SIZE
//
//            if (shouldPaginate) {
//                newsViewModel.onEvent(NewsEvent.LoadMoreSearchResults)
//            }
//        }
//
//        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            super.onScrollStateChanged(recyclerView, newState)
//            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) isScrolling = true
//        }
//    }
//
//    fun setupScrollListener() {
//        recyclerView.addOnScrollListener(scrollListener)
//    }
//}