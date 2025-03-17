package com.example.hazelnews.presentation.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hazelnews.R
import com.example.hazelnews.presentation.adapters.NewsAdapter
import com.example.hazelnews.databinding.FragmentFavouritesBinding
import com.example.hazelnews.domain.models.Article
import com.example.hazelnews.presentation.viewmodel.NewsViewModel
import com.example.hazelnews.presentation.events.NewsEvent
import com.example.hazelnews.presentation.state.NewsState
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavouritesFragment : Fragment(R.layout.fragment_favourites) {

    private lateinit var binding: FragmentFavouritesBinding
    private val newsViewModel: NewsViewModel by viewModels({ requireActivity() }) // Shared ViewModel
    private lateinit var newsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavouritesBinding.bind(view)

        setupFavouritesRecycler()

        // Fetch saved articles when fragment starts
        newsViewModel.onEvent(NewsEvent.FetchSavedArticles)

        // Observe state for favorite articles
//        lifecycleScope.launchWhenStarted {
//            newsViewModel.state.collectLatest { state ->
//                when (state) {
//                    is NewsState.SavedArticlesState -> {
//                        newsAdapter.differ.submitList(state.articles)
//                    }
//                    is NewsState.Error -> {
//                        Snackbar.make(view, state.message, Snackbar.LENGTH_SHORT).show()
//                    }
//                    else -> {}
//                }
//            }
//        }
        viewLifecycleOwner.lifecycleScope.launch {
            newsViewModel.state.collectLatest { state ->
                when (state) {
                    is NewsState.SavedArticlesState -> {
                        Log.d("FavouritesFragment", "Retrieved articles: ${state.articles}")
                        if (state.articles.isEmpty()) {
                            binding.recyclerFavourites.visibility = View.GONE
                            binding.emptyStateTextView.visibility = View.VISIBLE
                        } else {
                            binding.recyclerFavourites.visibility = View.VISIBLE
                            binding.emptyStateTextView.visibility = View.GONE
                            newsAdapter.differ.submitList(state.articles) // ✅ Update the list
                        }
                    }
                    is NewsState.Error -> {
                        Snackbar.make(requireView(), state.message, Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }


        // Handle item click to open ArticleFragment
        newsAdapter.setOnItemClickListener {
            val action = FavouritesFragmentDirections.actionFavouritesFragment2ToArticleFragment(it)
            findNavController().navigate(action)
        }

        // Swipe-to-delete functionality
        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article: Article = newsAdapter.differ.currentList[position]

                newsViewModel.onEvent(NewsEvent.DeleteArticle(article))

                Snackbar.make(view, "Removed from favorites", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        newsViewModel.onEvent(NewsEvent.ToggleFavorite(article))
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerFavourites)
    }

    private fun setupFavouritesRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerFavourites.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}



















//package com.example.hazelnews.ui.fragments
//
//import android.content.Context
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.lifecycle.Observer
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.ItemTouchHelper
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.hazelnews.R
//import com.example.hazelnews.adapters.NewsAdapter
//import com.example.hazelnews.databinding.FragmentArticleBinding
//import com.example.hazelnews.databinding.FragmentFavouritesBinding
//import com.example.hazelnews.ui.NewsActivity
//import com.example.hazelnews.ui.NewsViewModel
//import com.google.android.material.snackbar.Snackbar
//
//
//class FavouritesFragment : Fragment(R.layout.fragment_favourites) {
//
//
//    lateinit var newsViewModel: NewsViewModel
//    lateinit var newsAdapter: NewsAdapter
//    lateinit var binding: FragmentFavouritesBinding
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
/////////////////
//
//
//
//        binding = FragmentFavouritesBinding.bind(view)
//
//
//
//
//        newsViewModel = (activity as NewsActivity).newsViewModel
//        setupFavouritesRecycler()
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
//            findNavController().navigate(R.id.action_favouritesFragment2_to_articleFragment,bundle)
//        }
//val itemTouchHelperCAllback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
//    override fun onMove(
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder,
//        target: RecyclerView.ViewHolder
//    ): Boolean {
//        return true
//    }
//
//    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        val position = viewHolder.adapterPosition
//        val article  = newsAdapter.differ.currentList[position]
//        newsViewModel.deleteArticle(article)
//        Snackbar.make(view,"Removed from favourites", Snackbar.LENGTH_LONG).apply {
//            setAction("Undo") {
//                newsViewModel.addToFavourites(article)
//            }
//            show()
//        }
//
//    }
//
//}
//         ItemTouchHelper(itemTouchHelperCAllback).apply {
//             attachToRecyclerView(binding.recyclerFavourites)
//         }
//      newsViewModel.getFavouriteNews().observe(viewLifecycleOwner, Observer  { articles ->
//
//          newsAdapter.differ.submitList((articles))
//      })
//
//
//        newsViewModel.favoriteArticles.observe(viewLifecycleOwner, Observer { articles ->
//            newsAdapter.differ.submitList(articles) // Ensure RecyclerView updates properly
//        })
//    }
//    private fun setupFavouritesRecycler() {
//        newsAdapter = NewsAdapter()
//        binding.recyclerFavourites.apply {
//            adapter = newsAdapter
//
//            layoutManager = LinearLayoutManager(activity) // Use requireContext()
//
//
//        }
//    }
//
//}