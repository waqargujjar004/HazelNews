package com.example.hazelnews.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
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
        WindowCompat.setDecorFitsSystemWindows(window, true)
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


//class NewsActivity : BaseActivityWithVM<NewsViewModel, ActivityNewsBinding>(
//    viewModelClass = NewsViewModel::class.java,
//    bindingFactory = ActivityNewsBinding::inflate
//) {
//    private var navController: NavController? = null
//
//    override fun ActivityNewsBinding.bindViews() {
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
//        navController = navHostFragment.navController
//        bottomNavigationView.setupWithNavController(navController!!)
//    }
//
//    override fun ActivityNewsBinding.bindListeners() {
//        navController?.addOnDestinationChangedListener { _, destination, _ ->
//            if (destination.id == R.id.articleFragment) {
//                binding.bottomNavigationView.visibility = View.GONE
//            } else {
//                binding.bottomNavigationView.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    override fun ActivityNewsBinding.bindObservers() {
//
//    }
//
//}


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
