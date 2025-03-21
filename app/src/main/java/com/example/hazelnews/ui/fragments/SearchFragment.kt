
package com.example.hazelnews.ui.fragments
import androidx.fragment.app.activityViewModels
import com.hazelmobile.cores.bases.fragment.BaseFragment
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hazelnews.R
import com.example.hazelnews.ui.adapters.NewsAdapter
import com.example.hazelnews.databinding.FragmentSearchBinding
import com.example.hazelnews.ui.viewmodel.NewsViewModel
import com.example.hazelnews.ui.events.NewsEvent
import com.example.hazelnews.ui.fragments.SearchFragmentDirections.Companion.actionSearchFragment2ToArticleFragment
import com.example.hazelnews.ui.state.NewsState
import com.example.hazelnews.util.Constants
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


// SearchQueryHandler.kt
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

// PaginationHandler.kt
class PaginationHandler(
    private var newsViewModel: NewsViewModel,
    private var recyclerView: RecyclerView,
    private var isLastPage: Boolean,
    private var isScrolling: Boolean
) {
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val shouldPaginate = !isLastPage && isScrolling &&
                    firstVisibleItemPosition + visibleItemCount >= totalItemCount &&
                    firstVisibleItemPosition >= 0 &&
                    totalItemCount >= Constants.QUERY_PAGE_SIZE

            if (shouldPaginate) {
                newsViewModel.onEvent(NewsEvent.LoadMoreSearchResults)
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
}

// SearchFragment.kt
@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {

    private val newsViewModel: NewsViewModel by viewModels() // Shared ViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var searchQueryHandler: SearchQueryHandler
    private lateinit var paginationHandler: PaginationHandler

    private var isLastPage = false
    private var isScrolling = false

    override fun onViewBindingCreated(savedInstanceState: Bundle?) {
        super.onViewBindingCreated(savedInstanceState)
        searchQueryHandler = SearchQueryHandler(newsViewModel, lifecycleScope)
        binding?.recyclerSearch?.let { recyclerView ->
            paginationHandler = PaginationHandler(newsViewModel, recyclerView, isLastPage, isScrolling)
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
                val action = SearchFragmentDirections.actionSearchFragment2ToArticleFragment(article)
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
        if (isLastPage) binding?.recyclerSearch?.setPadding(0, 0, 0, 0)
    }

    private fun hideKeyboard() {
        binding?.searchEdit?.let { editText ->
            editText.clearFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.searchEdit?.removeTextChangedListener(searchTextWatcher)
    }
}









// before srp
//@AndroidEntryPoint
//class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {
//
//    private val newsViewModel: NewsViewModel by viewModels()// Shared ViewModel
//    private lateinit var newsAdapter: NewsAdapter
//    private var searchJob: Job? = null
//
//    private var isLastPage = false
//    private var isScrolling = false
//
//    override fun onViewBindingCreated(savedInstanceState: Bundle?) {
//        super.onViewBindingCreated(savedInstanceState)
//        setupRecyclerView()
//    }
//
//    override fun FragmentSearchBinding.bindListeners() {
//        searchEdit.addTextChangedListener(searchTextWatcher)
//
//        searchEdit.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                val query = searchEdit.text.toString().trim()
//                if (query.isNotEmpty()) {
//                    searchJob?.cancel()
//                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
//                        delay(200)
//                        newsViewModel.onEvent(NewsEvent.SearchNews(query))
//                    }
//                    hideKeyboard()
//                }
//                true
//            } else {
//                false
//            }
//        }
//    }
//
//    override fun FragmentSearchBinding.bindObservers() {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                newsViewModel.searchQuery
//                    .debounce(500)
//                    .distinctUntilChanged()
//                    .collect { query ->
//                        if (query.isNotEmpty()) {
//                            newsViewModel.onEvent(NewsEvent.SearchNews(query))
//                        }
//                    }
//            }
//        }
//
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                newsViewModel.state.collectLatest { state -> handleState(state) }
//            }
//        }
//    }
//
//    private fun handleState(state: NewsState) {
//        when (state) {
//            is NewsState.SearchResults -> {
//                hideProgressBar()
//                newsAdapter.submitList(state.articles)
//                isLastPage = state.isLastPage
//                adjustRecyclerPadding()
//            }
//            is NewsState.Loading ->
//                if (newsViewModel.searchQuery.value.isNotEmpty()) {  // Only show if a query is present
//                    showProgressBar()
//                }
//            is NewsState.Error -> {
//                hideProgressBar()
//                showError(state.message)
//            }
//            else -> Unit
//        }
//    }
//
//    private fun setupRecyclerView() {
//        newsAdapter = NewsAdapter()
//        binding?.recyclerSearch?.apply {
//            adapter = newsAdapter
//            layoutManager = LinearLayoutManager(requireContext())
//            addOnScrollListener(scrollListener)
//        }
//
//        setupListeners()
////        newsAdapter.onItemClickListener{ article ->
////            val action = SearchFragmentDirections.actionSearchFragment2ToArticleFragment(article)
////            findNavController().navigate(action)
////        }
//    }
//    fun setupListeners() {
//        newsAdapter.onItemClickListener { article ->
//            article?.let {
//                val action = SearchFragmentDirections.actionSearchFragment2ToArticleFragment(article)
//                findNavController().navigate(action)
//            }
//        }
//    }
//
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
//                isScrolling = false
//            }
//        }
//
//        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            super.onScrollStateChanged(recyclerView, newState)
//            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) isScrolling = true
//        }
//    }
//
//    private fun hideProgressBar() {
//        binding?.paginationProgressBar?.visibility = View.INVISIBLE
//    }
//
//    private fun showProgressBar() {
//        binding?.paginationProgressBar?.visibility = View.VISIBLE
//    }
//
//    private fun showError(message: String) {
//        binding?.root?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
//    }
//
//    private fun adjustRecyclerPadding() {
//        if (isLastPage) binding?.recyclerSearch?.setPadding(0, 0, 0, 0)
//    }
//
//    private fun hideKeyboard() {
//        binding?.searchEdit?.let { editText ->
//            editText.clearFocus()
//            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(editText.windowToken, 0)
//        }
//    }
//
//    private val searchTextWatcher = object : TextWatcher {
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//        override fun afterTextChanged(editable: Editable?) {
//            searchJob?.cancel()
//            searchJob = lifecycleScope.launch {
//                delay(500) // Avoid multiple API calls
//                val query = editable?.toString()?.trim()
//                if (!query.isNullOrEmpty() && query != newsViewModel.searchQuery.value) {
//                    Log.d("SearchFragment", "Searching for: $query")
//                    newsViewModel.onEvent(NewsEvent.SearchNews(query))
//                }
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding?.searchEdit?.removeTextChangedListener(searchTextWatcher)
//    }
//}
//
//
//













//class SearchFragment : BaseFragment<FragmentSearchBinding>( FragmentSearchBinding::inflate) {
//
//    private lateinit var newsAdapter: NewsAdapter
//    private var searchJob: Job? = null
//    private var isLastPage = false
//    private var isScrolling = false
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setupRecyclerView()
//        setupSearchInput()
//        observeViewModel()
//    }
//
//    private fun observeViewModel() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                newsViewModel.state.collectLatest { state ->
//                    handleState(state) // 🔥 Calls BaseFragment's handleState()
//                }
//            }
//        }
//    }
//
//    override fun handleState(state: NewsState) {
//        super.handleState(state) // ✅ Call BaseFragment's implementation for Error handling
//
//        when (state) {
//            is NewsState.SearchResults -> {
//                hideProgressBar()
//                newsAdapter.differ.submitList(state.articles)
//                isLastPage = state.isLastPage
//                adjustRecyclerPadding()
//            }
//            is NewsState.Loading -> showProgressBar()
//            else -> Log.d("SearchFragment", "Unhandled state: $state")
//        }
//    }
//
//    private fun setupSearchInput() {
//        binding.searchEdit.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                val query = binding.searchEdit.text.toString().trim()
//                if (query.isNotEmpty()) {
//                    searchJob?.cancel()
//                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
//                        delay(200)
//                        newsViewModel.onEvent(NewsEvent.SearchNews(query))
//                    }
//                    hideKeyboard()
//                }
//                true
//            } else {
//                false
//            }
//        }
//    }
//
//    private fun setupRecyclerView() {
//        newsAdapter = NewsAdapter()
//        binding.recyclerSearch.apply {
//            adapter = newsAdapter
//            layoutManager = LinearLayoutManager(requireContext())
//            addOnScrollListener(scrollListener)
//        }
//
//        newsAdapter.setOnItemClickListener { article ->
//            val action = SearchFragmentDirections.actionSearchFragment2ToArticleFragment(article)
//            findNavController().navigate(action)
//        }
//    }
//
//    private val scrollListener = object : RecyclerView.OnScrollListener() {
//        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            super.onScrolled(recyclerView, dx, dy)
//            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//            val visibleItemCount = layoutManager.childCount
//            val totalItemCount = layoutManager.itemCount
//
//            val shouldPaginate =
//                !isLastPage && isScrolling &&
//                        firstVisibleItemPosition + visibleItemCount >= totalItemCount &&
//                        firstVisibleItemPosition >= 0 &&
//                        totalItemCount >= Constants.QUERY_PAGE_SIZE
//
//            if (shouldPaginate) {
//                newsViewModel.onEvent(NewsEvent.LoadMoreSearchResults)
//                isScrolling = false
//            }
//        }
//
//        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            super.onScrollStateChanged(recyclerView, newState)
//            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) isScrolling = true
//        }
//    }
//
//    private fun hideProgressBar() {
//        binding.paginationProgressBar.visibility = View.INVISIBLE
//    }
//
//    private fun showProgressBar() {
//        binding.paginationProgressBar.visibility = View.VISIBLE
//    }
//
//    private fun adjustRecyclerPadding() {
//        if (isLastPage) binding.recyclerSearch.setPadding(0, 0, 0, 0)
//    }
//
//    private fun hideKeyboard() {
//        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(binding.searchEdit.windowToken, 0)
//        binding.searchEdit.clearFocus()
//    }
//}
//























//class SearchFragment : Fragment(R.layout.fragment_search) {
//
//    private var _binding: FragmentSearchBinding? = null
//    private val binding get() = _binding!!
//
//    private val newsViewModel: NewsViewModel by viewModels({ requireActivity() }) // Shared ViewModel
//    private lateinit var newsAdapter: NewsAdapter
//    private var searchJob: Job? = null
//
//    private var isLastPage = false
//    private var isScrolling = false
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        _binding = FragmentSearchBinding.bind(view)
//
//        setupRecyclerView()
//        setupSearchInput()
//        observeViewModel()
//        //handleRetryClick()
//
//        lifecycleScope.launch {
//            newsViewModel.searchQuery
//                .debounce(500) // 🔥 Wait for typing to stop
//                .distinctUntilChanged() // 🔥 Prevent duplicate calls
//                .collect { query ->
//                    if (query.isNotEmpty()) {
//                        newsViewModel.onEvent(NewsEvent.SearchNews(query))
//                    }
//                }
//        }
//
//
//    }
//
//
//    private fun setupSearchInput() {
//        binding.searchEdit.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(editable: Editable?) {
//                searchJob?.cancel() // Cancel previous job
//                searchJob = lifecycleScope.launch {
//                    delay(500) // 🔥 Debounce to prevent API spam
//                    editable?.toString()?.takeIf { it.isNotEmpty() }?.let {
//                        Log.d("SearchFragment", "Typing Detected: Searching -> $it") // Debugging
//                        newsViewModel.onEvent(NewsEvent.SearchNews(it))
//                    }
//                }
//            }
//        })
//        binding.searchEdit.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                val query = binding.searchEdit.text.toString().trim()
//                if (query.isNotEmpty()) {
//                    Log.d("SearchFragment", "Enter Key Pressed: Searching -> $query") // Debugging
//
//                    searchJob?.cancel() // Ensure previous job is canceled
//                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
//                        delay(200) // Optional: Small delay to prevent accidental multiple calls
//                        newsViewModel.onEvent(NewsEvent.SearchNews(query)) // 🔥 Perform search
//                    }
//
//                    // Hide keyboard after searching
//                    binding.searchEdit.clearFocus()
//                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.hideSoftInputFromWindow(binding.searchEdit.windowToken, 0)
//                }
//                true // Consume event
//            } else {
//                false // Default behavior
//            }
//        }
//
//
//        // 🔥 Perform search immediately when Enter key is pressed
////        binding.searchEdit.setOnEditorActionListener { _, actionId, _ ->
////            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
////                val query = binding.searchEdit.text.toString().trim()
////                if (query.isNotEmpty()) {
////                    Log.d("SearchFragment", "Enter Key Pressed: Searching -> $query") // Debugging
////                    searchJob?.cancel() // Cancel delayed search
////                    newsViewModel.onEvent(NewsEvent.SearchNews(query)) // 🔥 Instant search
////
////                    // 🔹 Hide the keyboard after searching
//////                    binding.searchEdit.clearFocus()
//////                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//////                    imm.hideSoftInputFromWindow(binding.searchEdit.windowToken, 0)
////                }
////                true // Consume event
////            } else {
////                false // Default behavior
////            }
////        }
//    }
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
//    //  var lastSearchQuery: String? = null
////    private fun setupSearchInput() {
////        Log.d("SearchFragment", "setupSearchInput() called") // Debug log
////        binding.searchEdit.addTextChangedListener { editable ->
////            searchJob?.cancel()
////            searchJob = lifecycleScope.launch {
////                delay(Constants.SEARCH_NEWS_TIME_DELAY)
////                editable?.toString()?.takeIf { it.isNotEmpty() }?.let {
////                    Log.d("SearchFragment", "Dispatching search event: $it") // Debug log
////                    newsViewModel.onEvent(NewsEvent.SearchNews(it))
////                }
////            }
////        }
////      binding.searchEdit.setOnEditorActionListener { _, actionId, _ ->
////          if (actionId == EditorInfo.IME_ACTION_SEARCH) {
////              val query = binding.searchEdit.text.toString().trim()
////              if (query.isNotEmpty()) {
////                  Log.d("SearchFragment", "Search button pressed. Query: $query")  // Debugging
////                  searchJob?.cancel()
////                  newsViewModel.onEvent(NewsEvent.SearchNews(query))
////              }
////              true
////          } else false
////      }
////
////    }
//
//    private fun observeViewModel() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                newsViewModel.state.collectLatest { state ->
//                    handleState(state)
//                }
//            }
//        }
//    }
//
//    private fun handleState(state: NewsState) {
//        Log.d("SearchFragment", "State received: $state")
//
//        when (state) {
//            is NewsState.SearchResults -> {
//                hideProgressBar()
//                newsAdapter.differ.submitList(state.articles)
//                isLastPage = state.isLastPage
//                adjustRecyclerPadding()
//            }
//            is NewsState.Loading -> {
//                Log.d("SearchFragment", "Showing progress bar")
//                showProgressBar()
//            }
//            is NewsState.Error -> {
//                hideProgressBar()
//                showError(state.message)
//                Log.e("SearchFragment", "Error: ${state.message}")
//            }
//            else -> Log.d("SearchFragment", "Unhandled state: $state")
//        }
//    }
//
//
//
//    private fun setupRecyclerView() {
//        newsAdapter = NewsAdapter()
//        binding.recyclerSearch.apply {
//            adapter = newsAdapter
//            layoutManager = LinearLayoutManager(requireContext())
//            addOnScrollListener(scrollListener)
//        }
//
//        newsAdapter.setOnItemClickListener { article ->
//            val action = SearchFragmentDirections.actionSearchFragment2ToArticleFragment(article)
//            findNavController().navigate(action)
//        }
//    }
//
//    private val scrollListener = object : RecyclerView.OnScrollListener() {
//        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            super.onScrolled(recyclerView, dx, dy)
//            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//            val visibleItemCount = layoutManager.childCount
//            val totalItemCount = layoutManager.itemCount
//
//            val shouldPaginate =
//                !isLastPage && isScrolling &&
//                        firstVisibleItemPosition + visibleItemCount >= totalItemCount &&
//                        firstVisibleItemPosition >= 0 &&
//                        totalItemCount >= Constants.QUERY_PAGE_SIZE
//
//            if (shouldPaginate) {
//                newsViewModel.onEvent(NewsEvent.LoadMoreSearchResults)
//                isScrolling = false
//            }
//        }
//
//        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            super.onScrollStateChanged(recyclerView, newState)
//            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) isScrolling = true
//        }
//    }
//
//    private fun hideProgressBar() {
//        binding.paginationProgressBar.visibility = View.INVISIBLE
//    }
//
//    private fun showProgressBar() {
//        binding.paginationProgressBar.visibility = View.VISIBLE
//    }
//
//    private fun showError(message: String) {
//        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
//    }
//
//    private fun adjustRecyclerPadding() {
//        if (isLastPage) binding.recyclerSearch.setPadding(0, 0, 0, 0)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding.searchEdit.addTextChangedListener(null)
//        _binding = null
//    }
//}

















//package com.example.hazelnews.ui.fragments
//
//import android.content.Context
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.AbsListView
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.cardview.widget.CardView
//import androidx.core.widget.addTextChangedListener
//import androidx.lifecycle.Observer
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.hazelnews.R
//import com.example.hazelnews.adapters.NewsAdapter
//import com.example.hazelnews.databinding.FragmentFavouritesBinding
//import com.example.hazelnews.databinding.FragmentSearchBinding
//import com.example.hazelnews.ui.NewsActivity
//import com.example.hazelnews.ui.NewsViewModel
//import com.example.hazelnews.util.Constants
//import com.example.hazelnews.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
//import com.example.hazelnews.util.Resource
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.MainScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//
//class SearchFragment : Fragment(R.layout.fragment_search) {
//
//    lateinit var newsViewModel: NewsViewModel
//    lateinit var newsAdapter: NewsAdapter
//    lateinit var retryButton: Button
//    lateinit var errorText: TextView
//    lateinit var itemSearchError: CardView
//    lateinit var binding: FragmentSearchBinding
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding = FragmentSearchBinding.bind(view)
//
//        itemSearchError = view.findViewById(R.id.itemSearchError)
//
//        val inflater =
//            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val view: View = inflater.inflate(R.layout.item_error, null)
//
//        retryButton = view.findViewById(R.id.retryButton)
//        errorText = view.findViewById(R.id.errorText)
//
//        newsViewModel = (activity as NewsActivity).newsViewModel
//        setupSearchRecycler()
//
//        newsAdapter.setOnItemClickListener {
//
//        }
//        newsAdapter.setOnItemClickListener {
//            val bundle = Bundle().apply {
//                putSerializable("article", it)
//            }
//
//            //
//            findNavController().navigate(R.id.action_searchFragment2_to_articleFragment, bundle)
//        }
//
//        var job: Job? = null
//
//        binding.searchEdit.addTextChangedListener { editable ->
//            job?.cancel()
//            job = MainScope().launch {
//                delay(SEARCH_NEWS_TIME_DELAY)
//                editable?.let {
//                    if (editable.toString().isNotEmpty()) {
//                        newsViewModel.searchNews(editable.toString())
//                    }
//                }
//            }
//        }
//
//        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
//            when (response) {
//                is Resource.Success<*> -> {
//                    hideProgressBar()
//                    hideErrorMessage()
//                    response.data?.let { newsResponse ->
//                        newsAdapter.differ.submitList(newsResponse.articles.toList())
//                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
//                        isLastPage = newsViewModel.searchNewsPage == totalPages
//                        if (isLastPage) {
//                            binding.recyclerSearch.setPadding(0, 0, 0, 0)
//                        }
//
//
//                    }
//                }
//
//                is Resource.Error<*> -> {
//                    // Handle error case
//                    hideProgressBar()
//                    response.message?.let { message ->
//                        Toast.makeText(activity, "Sorry error: $message", Toast.LENGTH_LONG).show()
//                        showErrorMessage(message)
//                    }
//
//                }
//
//                is Resource.Loading<*> -> {
//                    showProgressBar()
//                }
//            }
//
//        })
//
//        retryButton.setOnClickListener{
//            if (binding.searchEdit.text.toString().isNotEmpty()){
//                newsViewModel.searchNews(binding.searchEdit.text.toString())
//            }else{
//                hideErrorMessage()
//            }
//        }
//
//    }
//
//
//    var isError = false
//    var isLoading = false
//    var isLastPage = false
//    var isScrolling = false
//
//    private fun hideProgressBar() {
//        binding.paginationProgressBar.visibility = View.INVISIBLE
//        isLoading = false
//    }
//    private fun showProgressBar(){
//        binding.paginationProgressBar.visibility = View.VISIBLE
//        isLoading = true
//    }
//
//    private fun hideErrorMessage(){
//        itemSearchError.visibility = View.INVISIBLE
//        isError = false
//    }
//    private fun showErrorMessage(message: String ){
//        itemSearchError.visibility = View.VISIBLE
//        errorText.text = message
//        isError = true
//    }
//
//    val scrollListener = object : RecyclerView.OnScrollListener() {
//
//        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            super.onScrolled(recyclerView, dx, dy)
//            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//            val visibleItemCount = layoutManager.childCount
//            val totalItemCount = layoutManager.itemCount
//
//            val isNoErrors = !isError
//            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
//            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
//            val isNotAtBeginning = firstVisibleItemPosition >= 0
//
//            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
//            val shouldPaginate =
//                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
//
//            if (shouldPaginate) {
//                newsViewModel.searchNews(binding.searchEdit.text.toString())
//                isScrolling = false
//            }
//        }
//
//        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            super.onScrollStateChanged(recyclerView, newState)
//            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                isScrolling = true
//            }
//        }
//    }
//
// private fun setupSearchRecycler() {
//     newsAdapter = NewsAdapter()
//     binding.recyclerSearch.apply {
//         adapter = newsAdapter
//
//         layoutManager = LinearLayoutManager(activity) // Use requireContext()
//         addOnScrollListener(this@SearchFragment.scrollListener)
//     }
// }
//
//}