
package com.example.hazelnews.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.hazelnews.R
import com.example.hazelnews.databinding.ActivityNewsBinding
import com.example.hazelnews.presentation.viewmodel.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding
    private val newsViewModel: NewsViewModel by viewModels() // Inject ViewModel using Hilt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Bottom Navigation with Navigation Component
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        // Hide bottom navigation when ArticleFragment is opened
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.articleFragment) {
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }
}


















//package com.example.hazelnews.ui
//
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.example.hazelnews.R
//import androidx.lifecycle.ViewModelProvider
//import androidx.navigation.fragment.NavHostFragment
//import androidx.navigation.ui.setupWithNavController
//
//import com.example.hazelnews.databinding.ActivityNewsBinding
//import com.example.hazelnews.data.local.db.ArticleDatabase
//import com.example.hazelnews.data.repository.NewsRepository
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class NewsActivity : AppCompatActivity() {
//
//
//    lateinit var newsViewModel: NewsViewModel
//    lateinit var binding: ActivityNewsBinding
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityNewsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//        val newsRepository = NewsRepository(ArticleDatabase(this))
//
//        val viewModelProviderFactory = NewsViweModelProviderFactory(application,newsRepository)
//        newsViewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)
//
//
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
//        val navController = navHostFragment.navController
//        binding.bottomNavigationView.setupWithNavController(navController)
//        }
//    }
