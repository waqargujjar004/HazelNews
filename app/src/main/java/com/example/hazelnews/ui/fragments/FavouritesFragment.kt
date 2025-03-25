package com.example.hazelnews.ui.fragments

import com.hazelmobile.cores.bases.fragment.BaseFragment
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hazelnews.ui.adapters.NewsAdapter
import com.example.hazelnews.databinding.FragmentFavouritesBinding
import com.example.hazelnews.ui.viewmodel.NewsViewModel
import com.example.hazelnews.ui.events.NewsEvent
import com.example.hazelnews.ui.state.NewsState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavouritesFragment :
    BaseFragment<FragmentFavouritesBinding>(FragmentFavouritesBinding::inflate) {

    private val viewModel: NewsViewModel by viewModels()
    private lateinit var newsAdapter: NewsAdapter

    override fun FragmentFavouritesBinding.bindViews() {
        setupFavouritesRecycler()
    }

    override fun FragmentFavouritesBinding.bindListeners() {
        setupListeners()
    }

    override fun FragmentFavouritesBinding.bindObservers() {
        observeViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        viewModel.onEvent(NewsEvent.FetchSavedArticles)

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is NewsState.SavedArticlesState -> {
                        Log.d("FavouritesFragment", "Retrieved articles: ${state.articles}")

                        binding?.let { binding ->
                            binding.recyclerFavourites.isVisible = state.articles.isNotEmpty()
                            binding.emptyStateTextView.isVisible = state.articles.isEmpty()
                            // newsAdapter.submitList(state.articles)

                            if (::newsAdapter.isInitialized) {
                                newsAdapter.submitList(state.articles)
                            } else {
                                Log.e("FavouritesFragment", "Adapter is not initialized yet!")
                            }
                        }
                    }

                    is NewsState.Error -> {
                        Snackbar.make(requireView(), state.message, Snackbar.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun setupListeners() {
        newsAdapter.onItemClickListener { article ->
            article?.let {
                val action = FavouritesFragmentDirections
                    .actionFavouritesFragment2ToArticleFragment(it)
                findNavController().navigate(action)
            }
        }

        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val article = newsAdapter.currentList.getOrNull(viewHolder.adapterPosition)

                article?.let {
                    viewModel.onEvent(NewsEvent.DeleteArticle(it))

                    Snackbar.make(
                        viewHolder.itemView,
                        "Removed from favorites",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Undo") {
                            viewModel.onEvent(NewsEvent.ToggleFavorite(article))
                        }
                        .show()
                }
            }
        }
        binding?.let { binding ->
            ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerFavourites)
        }
    }

    private fun setupFavouritesRecycler() {
        newsAdapter = NewsAdapter()
        binding?.recyclerFavourites?.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())

        }
    }
}







