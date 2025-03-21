package com.example.hazelnews.ui.fragments


import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hazelnews.R
import com.example.hazelnews.databinding.FragmentArticleBinding
import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.ui.viewmodel.NewsViewModel
import com.example.hazelnews.ui.events.NewsEvent
import com.example.hazelnews.ui.state.NewsState
import com.google.android.material.snackbar.Snackbar
import com.hazelmobile.cores.bases.fragment.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

import kotlinx.coroutines.launch

@AndroidEntryPoint

class ArticleFragment : BaseFragment<FragmentArticleBinding>(FragmentArticleBinding::inflate) {

    private val viewModel: NewsViewModel by viewModels()
    private var isFavorite: Boolean = false
    private lateinit var article: Article

    override fun FragmentArticleBinding.bindViews() {
        // Get article data from arguments
        val args = ArticleFragmentArgs.fromBundle(requireArguments())
        article = args.article




        webView.apply {
            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    super.onPageCommitVisible(view, url)
                    webViewProgressBar.visibility =
                        View.GONE // ✅ Hide when content starts appearing
                }
            }

            article.url?.let { loadUrl(it) }
        }

//        webView.apply {
//            settings.javaScriptEnabled = true  // Enable JavaScript for modern websites
//
//            webViewClient = WebViewClient()  // Handles navigation inside the WebView
//
//            webChromeClient = object : WebChromeClient() {
//                override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                    if (newProgress < 50) {
//                        webViewProgressBar.visibility = View.VISIBLE  // Show progress bar when loading
//                    } else {
//                        webViewProgressBar.visibility = View.GONE  // Hide progress bar when loading is complete
//                    }
//                }
//            }
//
//            article.url?.let { loadUrl(it) }
//        }

//        // Load article in WebView
//        webView.apply {
//            webViewClient = WebViewClient()
//            article.url?.let { loadUrl(it) }
//        }

        // Dispatch event to check if the article is already a favorite
        article.url?.let { url ->
            viewModel.onEvent(NewsEvent.CheckFavoriteStatus(url))
        }
    }

    override fun FragmentArticleBinding.bindObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is NewsState.ArticleFavoriteState -> {
                        isFavorite = state.isFavorite
                        updateFabIcon(isFavorite)
                    }
                    is NewsState.Error -> {
                        Snackbar.make(root, state.message, Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun FragmentArticleBinding.bindListeners() {
        fab.setOnClickListener {
            if (!isFavorite) {
                viewModel.onEvent(NewsEvent.ToggleFavorite(article))
                isFavorite = true
                updateFabIcon(isFavorite)
                Snackbar.make(root, "Added to Favorites", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(root, "Already in Favorites", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun FragmentArticleBinding.updateFabIcon(isFavorite: Boolean) {
        fab.setImageResource(
            if (isFavorite) R.drawable.baseline_ffavorite_24 else R.drawable.baseline_favorite_24
        )
    }
}









//class ArticleFragment : BaseFragment<FragmentArticleBinding>(FragmentArticleBinding::inflate) {
//
//    private var isFavorite: Boolean = false
//    private lateinit var article: Article
//
//    private val viewModel: NewsViewModel by activityViewModels<NewsViewModel>()
//
//    override fun FragmentArticleBinding.bindViews() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Retrieve article from arguments
//        val args = ArticleFragmentArgs.fromBundle(requireArguments())
//        article = args.article
//
//        setupWebView()
//        observeFavoriteStatus()
//
//        binding.fab.setOnClickListener {
//            handleFavoriteClick()
//        }
//    }
//
//    private fun setupWebView() {
//        binding.webView.apply {
//            webViewClient = WebViewClient()
//            article.url?.let { loadUrl(it) }
//        }
//    }
//
//    private fun observeFavoriteStatus() {
//        article.url?.let { newsViewModel.onEvent(NewsEvent.CheckFavoriteStatus(it)) }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            newsViewModel.state.collectLatest { state ->
//                when (state) {
//                    is NewsState.ArticleFavoriteState -> {
//                        isFavorite = state.isFavorite
//                        updateFabIcon()
//                    }
//
//                    is NewsState.Error -> showSnackbar(state.message)
//                    else -> {}
//                }
//            }
//        }
//    }
//
//    private fun handleFavoriteClick() {
//        if (!isFavorite) {
//            newsViewModel.onEvent(NewsEvent.ToggleFavorite(article))
//            isFavorite = true
//            updateFabIcon()
//            showSnackbar("Added to Favorites")
//        } else {
//            showSnackbar("Already in Favorites")
//        }
//    }
//
//    private fun updateFabIcon() {
//        binding.fab.setImageResource(
//            if (isFavorite) R.drawable.baseline_ffavorite_24 else R.drawable.baseline_favorite_24
//        )
//    }
//
//    private fun showSnackbar(message: String) {
//        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
//    }
//}


//class ArticleFragment : Fragment(R.layout.fragment_article) {
//
//    private lateinit var binding: FragmentArticleBinding
//    private val newsViewModel: NewsViewModel by viewModels({ requireActivity() }) // Using ViewModel scoped to Activity
//    private var isFavorite: Boolean = false
//    private lateinit var article: Article
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding = FragmentArticleBinding.bind(view)
//
//        lifecycleScope.launchWhenStarted {
//            newsViewModel.state.collectLatest { state ->
//                when (state) {
//                    is NewsState.ArticleFavoriteState -> {
//                        isFavorite = state.isFavorite
//                        updateFabIcon(isFavorite)  // ✅ Update FAB icon
//                    }
//                    is NewsState.Error -> {
//                        Snackbar.make(view, state.message, Snackbar.LENGTH_SHORT).show()
//                    }
//                    else -> {}
//                }
//            }
//        }
//
//        // Get article data from arguments
//        val args = ArticleFragmentArgs.fromBundle(requireArguments())
//        article = args.article
//
//        // Load article in WebView
//        binding.webView.apply {
//            webViewClient = WebViewClient()
//            article.url?.let { loadUrl(it) }
//        }
//
//        // Dispatch event to check if the article is already a favorite
//        article.url?.let { url ->
//            newsViewModel.onEvent(NewsEvent.CheckFavoriteStatus(url))
//        }
//
//        // Observe ViewModel state for favorite status
//        lifecycleScope.launchWhenStarted {
//            newsViewModel.state.collectLatest { state ->
//                when (state) {
//                    is NewsState.ArticleFavoriteState -> {
//                        isFavorite = state.isFavorite
//                        updateFabIcon(isFavorite)
//                    }
//
//                    is NewsState.Error -> {
//                        Snackbar.make(view, state.message, Snackbar.LENGTH_SHORT).show()
//                    }
//
//                    else -> {}
//                }
//            }
//        }
//
//        // Handle FAB click for adding/removing favorites
//        binding.fab.setOnClickListener {
//            if (!isFavorite) { // ✅ Only add if not already favorite
//                newsViewModel.onEvent(NewsEvent.ToggleFavorite(article))
//                isFavorite = true // ✅ Manually update state
//                updateFabIcon(isFavorite)
//                Snackbar.make(view, "Added to Favorites", Snackbar.LENGTH_SHORT).show()
//            } else {
//                Snackbar.make(view, "Already in Favorites", Snackbar.LENGTH_SHORT).show()
//            }
//        }
//    }
//    private fun updateFabIcon(isFavorite: Boolean) {
//        binding.fab.setImageResource(
//            if (isFavorite) R.drawable.baseline_ffavorite_24 else R.drawable.baseline_favorite_24
//        )
//    }
//}


//
//package com.example.hazelnews.ui.fragments
//import com.example.hazelnews.domain.models.Article
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.webkit.WebViewClient
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.navArgs
//import com.example.hazelnews.R
//import com.example.hazelnews.databinding.FragmentArticleBinding
//import com.example.hazelnews.ui.NewsActivity
//import com.example.hazelnews.ui.NewsViewModel
//import com.google.android.material.snackbar.Snackbar
//import kotlinx.coroutines.launch
//
//
//class ArticleFragment : Fragment(R.layout.fragment_article) {
//
//    private var isFavorite: Boolean = false
//    lateinit var newsViewModel: NewsViewModel
//    val args: ArticleFragmentArgs by navArgs()
//    lateinit var binding: FragmentArticleBinding
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding = FragmentArticleBinding.bind(view)
//
//        newsViewModel = (activity as NewsActivity).newsViewModel
//        val article = args.article
//
//        binding.webView.apply {
//            webViewClient = WebViewClient()
//            article.url?.let {
//                loadUrl(it)
//            }
//        }
//
//
//
//
//
//
//        article.url?.let { url ->
//            newsViewModel.isArticleExists(url).observe(viewLifecycleOwner) { isFav ->
//                isFavorite = isFav // Update the boolean variable
//                updateFabIcon(isFavorite) // Update FAB icon based on status
//            }
//        }
//
//
//
//        binding.fab.setOnClickListener {
////            newsViewModel.addToFavourites(article)
////            Snackbar.make(view, "Added To Favourites @@" , Snackbar.LENGTH_SHORT ).show()
//
//
////            newsViewModel.addToFavourites(article)
////            isFavorite = !isFavorite  // Toggle the favorite state
////            updateFabIcon(isFavorite)
////            val message = if (isFavorite) "Added to Favourites" else "Removed from Favourites"
////            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
//
//            if (isFavorite) {
//
//                Snackbar.make(view, "Already Favourite", Snackbar.LENGTH_SHORT).show()
//                // newsViewModel.removeFavorite(article)  // If already favorite, remove it
//            } else {
//                isFavorite = !isFavorite  // Toggle state
//                newsViewModel.addToFavourites(article)
//                updateFabIcon(isFavorite)// Otherwise, add to favorites
//                Snackbar.make(view, "Added to Favourites", Snackbar.LENGTH_SHORT).show()
//            }
//            // isFavorite = !isFavorite  // Toggle state
//
////
////            val message = if (isFavorite) "Added to Favourites" else "Removed from Favourites"
////            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
//
//        }
//    }
//
//    private fun updateFabIcon(isFavorite: Boolean) {
//        if (isFavorite) {
//            binding.fab.setImageResource(R.drawable.baseline_ffavorite_24) // Filled heart
//        } else {
//            binding.fab.setImageResource(R.drawable.baseline_favorite_24) // Border heart
//        }
//    }
//
//}