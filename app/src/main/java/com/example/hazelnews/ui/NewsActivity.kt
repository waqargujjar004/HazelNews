package com.example.hazelnews.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.hazelnews.R
import com.example.hazelnews.databinding.ActivityNewsBinding
import com.example.hazelnews.ui.viewmodel.NewsViewModel
import com.hazelmobile.cores.bases.activity.BaseActivityWithVM
import dagger.hilt.android.AndroidEntryPoint

//@AndroidEntryPoint
@AndroidEntryPoint
class NewsActivity : BaseActivityWithVM<NewsViewModel, ActivityNewsBinding>(
    NewsViewModel::class.java,
    ActivityNewsBinding::inflate
) {

    override fun onViewBindingCreated(savedInstanceState: Bundle?) {
        super.onViewBindingCreated(savedInstanceState)
//        WindowCompat.setDecorFitsSystemWindows(window, true)
        // Setup Bottom Navigation with Navigation Component
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val handled = NavigationUI.onNavDestinationSelected(item, navController)
            if (!handled) {
                navController.navigate(item.itemId)
            }
            true
        }

        // Hide bottom navigation when ArticleFragment is opened
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                if (destination.id == R.id.articleFragment) View.GONE else View.VISIBLE
        }
    }
}

