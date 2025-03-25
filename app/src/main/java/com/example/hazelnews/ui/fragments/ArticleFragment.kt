package com.example.hazelnews.ui.fragments

import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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


        // Dispatch event to check if the article is already a favorite
        article.url?.let { url ->
            viewModel.onEvent(NewsEvent.CheckFavoriteStatus(url))
        }
    }

    override fun FragmentArticleBinding.bindObservers() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
    }

    override fun FragmentArticleBinding.bindListeners() {
        fab.setOnClickListener {
            if (!isFavorite) {
                viewModel.onEvent(NewsEvent.ToggleFavorite(article))
                isFavorite = true
                updateFabIcon(isFavorite)
                Snackbar.make(root, "Added to Favorites", Snackbar.LENGTH_SHORT).show()

            } else {

                Snackbar.make(fab, "Already in Favorites", Snackbar.LENGTH_SHORT).show()


            }
        }
    }

    private fun FragmentArticleBinding.updateFabIcon(isFavorite: Boolean) {
        fab.setImageResource(
            if (isFavorite) R.drawable.baseline_ffavorite_24 else R.drawable.baseline_favorite_24
        )
    }
}

