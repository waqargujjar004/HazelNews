package com.example.hazelnews.ui.fragments

import com.hazelmobile.cores.bases.fragment.BaseFragment
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hazelnews.ui.adapters.NewsAdapter
import com.example.hazelnews.databinding.FragmentSearchBinding
import com.example.hazelnews.ui.viewmodel.NewsViewModel
import com.example.hazelnews.ui.events.NewsEvent
import com.example.hazelnews.ui.state.NewsState
import com.example.hazelnews.util.PaginationHandler
import com.example.hazelnews.util.SearchQueryHandler
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {

    private val newsViewModel: NewsViewModel by viewModels()
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var searchQueryHandler: SearchQueryHandler
    private lateinit var paginationHandler: PaginationHandler

    private var isLastPage = false


    override fun FragmentSearchBinding.bindViews() {

        searchQueryHandler = SearchQueryHandler(newsViewModel, lifecycleScope)
        recyclerSearch.let { recyclerView ->
            paginationHandler = PaginationHandler(
                recyclerView,
                isLastPageProvider = false,
                onLoadMore = { newsViewModel.onEvent(NewsEvent.LoadMoreSearchResults) }
            )

            setupRecyclerView()

        }
    }

    override fun FragmentSearchBinding.bindListeners() {
        searchEdit.addTextChangedListener(searchTextWatcher)

        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEdit.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchQueryHandler.handleSearch(query)
                    hideKeyboard()
                }
                true
            } else {
                false
            }
        }
    }

    override fun FragmentSearchBinding.bindObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.searchQuery
                    .debounce(500)
                    .distinctUntilChanged()
                    .collect { query -> searchQueryHandler.handleSearch(query) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.state.collectLatest { state -> handleState(state) }
            }
        }
    }

    private fun handleState(state: NewsState) {
        when (state) {
            is NewsState.SearchResults -> {
                hideProgressBar()
                newsAdapter.submitList(state.articles)
                isLastPage = state.isLastPage
                paginationHandler.updateLastPageState(isLastPage)
                adjustRecyclerPadding()
            }

            is NewsState.Loading -> {
                if (newsViewModel.searchQuery.value.isNotEmpty()) {
                    showProgressBar()
                }
            }

            is NewsState.Error -> {
                hideProgressBar()
                showError(state.message)
            }

            else -> Unit
        }
    }

    private fun setupRecyclerView() {

        newsAdapter = NewsAdapter()
        binding?.recyclerSearch?.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        paginationHandler.setupScrollListener()
        setupListeners()
    }

    private fun setupListeners() {
        newsAdapter.onItemClickListener { article ->
            article?.let {
                val action =
                    SearchFragmentDirections.actionSearchFragment2ToArticleFragment(article)
                findNavController().navigate(action)
            }
        }
    }

    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            searchQueryHandler.handleSearch(editable?.toString())
        }
    }

    private fun hideProgressBar() {
        binding?.paginationProgressBar?.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding?.paginationProgressBar?.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding?.root?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    private fun adjustRecyclerPadding() {
        if (isLastPage) binding?.recyclerSearch?.setPadding(0, 0, 0, if (isLastPage) 0 else 50)
    }

    private fun hideKeyboard() {
        binding?.searchEdit?.let { editText ->
            editText.clearFocus()
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.searchEdit?.removeTextChangedListener(searchTextWatcher)
    }
}







