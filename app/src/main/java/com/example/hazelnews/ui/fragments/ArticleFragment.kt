package com.example.hazelnews.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.hazelnews.R
import com.example.hazelnews.databinding.FragmentArticleBinding
import com.example.hazelnews.ui.NewsActivity
import com.example.hazelnews.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class ArticleFragment : Fragment(R.layout.fragment_article) {

    private var isFavorite: Boolean = false
    lateinit var newsViewModel: NewsViewModel
    val args: ArticleFragmentArgs by navArgs()
    lateinit var binding: FragmentArticleBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)

        newsViewModel = (activity as NewsActivity).newsViewModel
        val article = args.article

        binding.webView.apply {
             webViewClient = WebViewClient()
            article.url?.let {
                loadUrl(it)
            }
        }






        article.url?.let { url ->
            newsViewModel.isArticleExists(url).observe(viewLifecycleOwner) { isFav ->
                isFavorite = isFav // Update the boolean variable
                updateFabIcon(isFavorite) // Update FAB icon based on status
            }
        }



        binding.fab.setOnClickListener {
//            newsViewModel.addToFavourites(article)
//            Snackbar.make(view, "Added To Favourites @@" , Snackbar.LENGTH_SHORT ).show()


//            newsViewModel.addToFavourites(article)
//            isFavorite = !isFavorite  // Toggle the favorite state
//            updateFabIcon(isFavorite)
//            val message = if (isFavorite) "Added to Favourites" else "Removed from Favourites"
//            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()

            if (isFavorite) {

                Snackbar.make(view, "Already Favourite", Snackbar.LENGTH_SHORT).show()
               // newsViewModel.removeFavorite(article)  // If already favorite, remove it
            } else {
                isFavorite = !isFavorite  // Toggle state
                newsViewModel.addToFavourites(article)
                updateFabIcon(isFavorite)// Otherwise, add to favorites
                Snackbar.make(view, "Added to Favourites", Snackbar.LENGTH_SHORT).show()
            }
           // isFavorite = !isFavorite  // Toggle state

//
//            val message = if (isFavorite) "Added to Favourites" else "Removed from Favourites"
//            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()

        }
    }

    private fun updateFabIcon(isFavorite: Boolean) {
        if (isFavorite) {
            binding.fab.setImageResource(R.drawable.baseline_ffavorite_24) // Filled heart
        } else {
            binding.fab.setImageResource(R.drawable.baseline_favorite_24) // Border heart
        }
    }

}