package com.example.hazelnews.ui.fragments

import android.os.Parcelable
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hazelnews.databinding.FragmentHeadlinesBinding
import com.example.hazelnews.ui.adapters.NewsAdapter
import com.example.hazelnews.ui.events.NewsEvent
import com.example.hazelnews.ui.state.NewsState
import com.example.hazelnews.ui.viewmodel.NewsViewModel
import com.example.hazelnews.util.Constants
import com.example.hazelnews.util.PaginationHandler
import com.hazelmobile.cores.bases.fragment.BaseFragment
import com.hazelmobile.cores.extensions.lazyAndroid
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HeadlinesFragment : BaseFragment<FragmentHeadlinesBinding>(
    FragmentHeadlinesBinding::inflate
) {
    private val viewModel: NewsViewModel by viewModels()
    private lateinit var newsAdapter: NewsAdapter
    private var recyclerViewState: Parcelable? = null
    private lateinit var paginationHandler: PaginationHandler
    private val stateHandler by lazyAndroid { NewsStateHandler() }

    override fun FragmentHeadlinesBinding.bindViews() {
        setupRecyclerView()

    }

    override fun FragmentHeadlinesBinding.bindObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    stateHandler.handleState(state, newsAdapter, binding, viewModel)
                    val isLastPage = (state as? NewsState.Success)?.isLastPage ?: false
                    paginationHandler.updateLastPageState(isLastPage)
                }
            }
        }
    }

    override fun FragmentHeadlinesBinding.bindListeners() {
        itemHeadlinesError.retryButton.setOnClickListener {
            viewModel.onEvent(NewsEvent.FetchHeadlines("us"))
        }
    }


    private fun FragmentHeadlinesBinding.setupRecyclerView() {
        newsAdapter = NewsAdapter().apply {
            onItemClickListener { article ->
                val action =
                    HeadlinesFragmentDirections.actionHeadlinesFragment2ToArticleFragment(article!!)
                findNavController().navigate(action)
            }
        }
        recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            //addOnScrollListener(paginationHelper.scrollListener(viewModel))
            paginationHandler = PaginationHandler(
                recyclerView = this,
                isLastPageProvider = false, // Provide isLastPage dynamically
                onLoadMore = { viewModel.onEvent(NewsEvent.FetchHeadlines("us")) } // Trigger the correct event
            )
            paginationHandler.setupScrollListener()
        }
    }

    override fun onPause() {
        super.onPause()
        recyclerViewState = binding?.recyclerHeadlines?.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        recyclerViewState?.let {
            binding?.recyclerHeadlines?.layoutManager?.onRestoreInstanceState(it)
        }
    }
}

class NewsStateHandler {
    fun handleState(
        state: NewsState,
        adapter: NewsAdapter,
        binding: FragmentHeadlinesBinding?,
        viewModel: NewsViewModel
    ) {
        when (state) {
            is NewsState.Success -> handleSuccess(state, adapter, binding, viewModel)
            is NewsState.Error -> handleError(state, binding)
            is NewsState.Loading -> showProgressBar(binding)
            is NewsState.ArticleFavoriteState -> {}
            is NewsState.SavedArticlesState -> {}
            is NewsState.SearchResults -> {}
        }
    }

    private fun handleSuccess(
        state: NewsState.Success,
        adapter: NewsAdapter,
        binding: FragmentHeadlinesBinding?,
        viewModel: NewsViewModel
    ) {
        hideProgressBar(binding)
        hideErrorMessage(binding)
        adapter.submitList(state.articles.toList())
        updatePagination(state.totalResults, binding, viewModel)
    }

    private fun handleError(state: NewsState.Error, binding: FragmentHeadlinesBinding?) {
        hideProgressBar(binding)
        showErrorMessage(state.message, binding)
    }

    private fun updatePagination(
        totalResults: Int,
        binding: FragmentHeadlinesBinding?,
        viewModel: NewsViewModel
    ) {
        val totalPages = totalResults / Constants.QUERY_PAGE_SIZE + 2
        val isLastPage = viewModel.getCurrentPage() == totalPages
        if (isLastPage) binding?.recyclerHeadlines?.setPadding(0, 0, 0, 0)
    }

    private fun hideProgressBar(binding: FragmentHeadlinesBinding?) {
        binding?.paginationProgressBar?.isVisible = false
    }

    private fun showProgressBar(binding: FragmentHeadlinesBinding?) {
        binding?.paginationProgressBar?.isVisible = true
    }

    private fun hideErrorMessage(binding: FragmentHeadlinesBinding?) {
        binding?.itemHeadlinesError?.root?.isVisible = false
    }

    private fun showErrorMessage(message: String, binding: FragmentHeadlinesBinding?) {
        binding?.itemHeadlinesError?.root?.isVisible = true
        binding?.itemHeadlinesError?.errorText?.text = message
    }
}
