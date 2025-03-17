package com.example.hazelnews.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hazelnews.R
import com.example.hazelnews.presentation.adapters.NewsAdapter
import com.example.hazelnews.databinding.FragmentHeadlinesBinding
import com.example.hazelnews.presentation.events.NewsEvent
import com.example.hazelnews.presentation.state.NewsState
import com.example.hazelnews.presentation.viewmodel.NewsViewModel
import com.example.hazelnews.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {

    private val newsViewModel: NewsViewModel by viewModels()
    private lateinit var newsAdapter: NewsAdapter
    private var _binding: FragmentHeadlinesBinding? = null
    private val binding get() = _binding!!

    private var isLoading = false
    private var isLastPage = false
    private var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeadlinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        setupHeadlinesRecycler()

        // Observe MVI State Flow
        lifecycleScope.launch {
            newsViewModel.state.collectLatest { state ->
                when (state) {
                    is NewsState.Success -> {
                        hideProgressBar()
                        hideErrorMessage()
                        state.articles.let { articles ->
                            newsAdapter.differ.submitList(articles.toList())
                            val totalPages = state.totalResults / Constants.QUERY_PAGE_SIZE + 2
                            isLastPage = newsViewModel.getCurrentPage() == totalPages
                            if (isLastPage) {
                                binding.recyclerHeadlines.setPadding(0, 0, 0, 0)
                            }
                        }
                    }
                    is NewsState.Error -> {
                        hideProgressBar()
                        showErrorMessage(state.message)
                    }
                    is NewsState.Loading -> {
                        showProgressBar()
                    }
                    else -> {}
                }
            }
        }

        // Retry Button Click Listener
        binding.itemHeadlinesError.retryButton.setOnClickListener {
            newsViewModel.onEvent(NewsEvent.FetchHeadlines("us"))
        }

        // Item Click Listener -> Navigate to ArticleFragment
        newsAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(R.id.action_headlinesFragment2_to_articleFragment, bundle)
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val shouldPaginate =
                !isLoading && !isLastPage && firstVisibleItemPosition + visibleItemCount >= totalItemCount
                        && firstVisibleItemPosition >= 0 && totalItemCount >= Constants.QUERY_PAGE_SIZE && isScrolling

            if (shouldPaginate) {
                newsViewModel.onEvent(NewsEvent.FetchHeadlines("us"))  // Fetch next page
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setupHeadlinesRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@HeadlinesFragment.scrollListener)
        }
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        binding.itemHeadlinesError.root.visibility = View.INVISIBLE
    }

    private fun showErrorMessage(message: String) {
        binding.itemHeadlinesError.root.visibility = View.VISIBLE
        binding.itemHeadlinesError.errorText.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}










//package com.example.hazelnews.ui.fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.AbsListView
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.hazelnews.R
//import com.example.hazelnews.adapters.NewsAdapter
//import com.example.hazelnews.databinding.FragmentHeadlinesBinding
//import com.example.hazelnews.ui.events.NewsEvent
//import com.example.hazelnews.ui.state.NewsState
//import com.example.hazelnews.ui.NewsViewModel
//import com.example.hazelnews.util.Constants
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//
//@AndroidEntryPoint
//class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {
//
//    private val newsViewModel: NewsViewModel by viewModels()
//    private lateinit var newsAdapter: NewsAdapter
//    private var _binding: FragmentHeadlinesBinding? = null
//    private val binding get() = _binding!!
//
//    private var isLoading = false
//    private var isLastPage = false
//    private var isScrolling = false
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentHeadlinesBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Setup RecyclerView
//        setupHeadlinesRecycler()
//
//        // Observe MVI State Flow
//        lifecycleScope.launch {
//            newsViewModel.state.collectLatest { state ->
//                when (state) {
//                    is NewsState.Success -> {
//                        hideProgressBar()
//                        hideErrorMessage()
//                        state.articles.let { articles ->
//                            newsAdapter.differ.submitList(articles.toList())
//                            val totalPages = state.totalResults / Constants.QUERY_PAGE_SIZE + 2
//                            isLastPage = newsViewModel.getCurrentPage() == totalPages
//                            if (isLastPage) {
//                                binding.recyclerHeadlines.setPadding(0, 0, 0, 0)
//                            }
//                        }
//                    }
//                    is NewsState.Error -> {
//                        hideProgressBar()
//                        showErrorMessage(state.message)
//                    }
//                    is NewsState.Loading -> {
//                        showProgressBar()
//                    }
//                    is NewsState.SavedArticlesState -> {
//                        // Handle saved articles state (if needed)
//                    }
//                    is NewsState.ArticleFavoriteState -> {
//                        // Handle article favorite state (if needed)
//                    }
//                    else -> {
//                        // Optional: Handle unexpected cases
//                    }
//                }
//            }
//        }
//
//        // Retry Button Click Listener
//        binding.itemHeadlinesError.retryButton.setOnClickListener {
//            newsViewModel.onEvent(NewsEvent.FetchHeadlines("us"))
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
//                !isLoading && !isLastPage && firstVisibleItemPosition + visibleItemCount >= totalItemCount
//                        && firstVisibleItemPosition >= 0 && totalItemCount >= Constants.QUERY_PAGE_SIZE && isScrolling
//
//            if (shouldPaginate) {
//                newsViewModel.onEvent(NewsEvent.FetchHeadlines("us"))  // Fetch next page
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
//    private fun setupHeadlinesRecycler() {
//        newsAdapter = NewsAdapter()
//        binding.recyclerHeadlines.apply {
//            adapter = newsAdapter
//            layoutManager = LinearLayoutManager(activity)
//            addOnScrollListener(this@HeadlinesFragment.scrollListener)
//        }
//    }
//
//
//    private fun hideProgressBar() {
//        binding.paginationProgressBar.visibility = View.INVISIBLE
//        isLoading = false
//    }
//
//    private fun showProgressBar() {
//        binding.paginationProgressBar.visibility = View.VISIBLE
//        isLoading = true
//    }
//
//    private fun hideErrorMessage() {
//        binding.itemHeadlinesError.root.visibility = View.INVISIBLE
//    }
//
//    private fun showErrorMessage(message: String) {
//        binding.itemHeadlinesError.root.visibility = View.VISIBLE
//        binding.itemHeadlinesError.errorText.text = message
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
//
//
//
//


















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
//import androidx.lifecycle.Observer
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.hazelnews.R
//import com.example.hazelnews.adapters.NewsAdapter
//import com.example.hazelnews.databinding.FragmentHeadlinesBinding
//import com.example.hazelnews.databinding.ItemErrorBinding
//import com.example.hazelnews.ui.NewsActivity
//import com.example.hazelnews.ui.NewsViewModel
//import com.example.hazelnews.util.Constants
//import com.example.hazelnews.util.Resource
//
//
//class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {
//
//    lateinit var newsViewModel: NewsViewModel
//    lateinit var newsAdapter: NewsAdapter
//    lateinit var retryButton: Button
//    lateinit var errorText: TextView
//    lateinit var itemHeadLinesError: CardView
//   // lateinit var itemErrorBinding: ItemErrorBinding
//    lateinit var binding: FragmentHeadlinesBinding
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding = FragmentHeadlinesBinding.bind(view)
//
//        itemHeadLinesError = view.findViewById(R.id.itemHeadlinesError)
//
//        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val view: View = inflater.inflate(R.layout.item_error,  null)
//
//
//        itemHeadLinesError = binding.itemHeadlinesError.root  // Access root of included layout
//        retryButton = binding.itemHeadlinesError.retryButton
//        errorText = binding.itemHeadlinesError.errorText
//
//
//
////         retryButton = view.findViewById(R.id.retryButton)
////        errorText = view.findViewById(R.id.errorText)
//
//        newsViewModel = (activity as NewsActivity).newsViewModel
//        setupHeadlinesRecycler()
//
//        newsAdapter.setOnItemClickListener {
//
//        }
//        newsAdapter.setOnItemClickListener {
//            val bundle = Bundle().apply {
//                putSerializable("article", it)
//            }
//
//   //
//            findNavController().navigate(R.id.action_headlinesFragment2_to_articleFragment,bundle)
//        }
//
//
//        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
//            when(response) {
//                is Resource.Success<*> -> {
//                    hideProgressBar()
//                    hideErrorMessage()
//                    response.data?.let { newsResponse ->
//                        newsAdapter.differ.submitList(newsResponse.articles.toList())
//                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
//                        isLastPage = newsViewModel.headlinesPage ==totalPages
//                        if (isLastPage)
//                        {
//                            binding.recyclerHeadlines.setPadding(0,0,0,0)
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
//                is Resource.Loading<*> -> {
//                   showProgressBar()
//                }
//            }
//
//        })
//
//
//     retryButton.setOnClickListener{
//         println("buton pressed ")
//     newsViewModel.getHeadlines("us")
//    }
//
//
//    }
//
//
//
//  var isError = false
//  var isLoading = false
//  var isLastPage = false
//  var isScrolling = false
//
//  private fun hideProgressBar() {
//      binding.paginationProgressBar.visibility = View.INVISIBLE
//      isLoading = false
//  }
//    private fun showProgressBar(){
//        binding.paginationProgressBar.visibility = View.VISIBLE
//        isLoading = true
//    }
//
//     private fun hideErrorMessage(){
//         itemHeadLinesError.visibility = View.INVISIBLE
//         isError = false
//     }
//    private fun showErrorMessage(message: String ){
//        itemHeadLinesError.visibility = View.VISIBLE
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
//                newsViewModel.getHeadlines("us")
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
//        private fun setupHeadlinesRecycler(){
//            newsAdapter = NewsAdapter()
//            binding.recyclerHeadlines.apply {
//                adapter = newsAdapter
//
//                layoutManager = LinearLayoutManager(activity) // Use requireContext()
//                addOnScrollListener(this@HeadlinesFragment.scrollListener)
//
//            }
//        }
//
//    }
//
//
//
//
//
