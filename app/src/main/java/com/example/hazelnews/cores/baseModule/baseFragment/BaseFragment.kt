//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.annotation.LayoutRes
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.repeatOnLifecycle
//import androidx.viewbinding.ViewBinding
//import com.example.hazelnews.ui.viewmodel.NewsViewModel
//import com.example.hazelnews.ui.state.NewsState
//import com.google.android.material.snackbar.Snackbar
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//import java.lang.reflect.Method
//import java.lang.reflect.ParameterizedType
//
//abstract class BaseFragment<VB : ViewBinding>(
//    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
//) : Fragment() {
//
//    private var _binding: VB? = null
//    protected val binding get() = _binding!!
//
//    protected val newsViewModel: NewsViewModel by viewModels({ requireActivity() })
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        _binding = bindingInflater(inflater, container, false)
//        return binding.root
////    }  return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        observeViewModel()
//    }
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
//    open fun handleState(state: NewsState) {
//        if (state is NewsState.Error) {
//            showError(state.message)
//        }
//    }
//
//    protected fun showError(message: String) {
//        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null // Prevent memory leaks
//    }
//}
//
//
//
//
//
//
//
//
////import android.os.Bundle
////import android.view.View
////import androidx.annotation.LayoutRes
////import androidx.fragment.app.Fragment
////import androidx.fragment.app.viewModels
////import androidx.lifecycle.Lifecycle
////import androidx.lifecycle.lifecycleScope
////import androidx.lifecycle.repeatOnLifecycle
////import com.example.hazelnews.ui.viewmodel.NewsViewModel
////import com.example.hazelnews.ui.state.NewsState
////import com.google.android.material.snackbar.Snackbar
////import kotlinx.coroutines.flow.collectLatest
////import kotlinx.coroutines.launch
////
////abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {
////
////    protected val newsViewModel: NewsViewModel by viewModels({ requireActivity() })
////
////    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
////        super.onViewCreated(view, savedInstanceState)
////        observeViewModel()
////    }
////
////    private fun observeViewModel() {
////        viewLifecycleOwner.lifecycleScope.launch {
////            repeatOnLifecycle(Lifecycle.State.STARTED) {
////                newsViewModel.state.collectLatest { state ->
////                    handleState(state)
////                }
////            }
////        }
////    }
////
////    open fun handleState(state: NewsState) {
////        if (state is NewsState.Error) {
////            showError(state.message)
////        }
////    }
////
////    protected fun showError(message: String) {
////        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
////    }
////}
