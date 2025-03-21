//package com.hazelmobile.cores.bases.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.hazelnews.util.NetworkHelper
//import kotlinx.coroutines.CoroutineDispatcher
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//abstract class BaseViewModel<T : Any>(
//    private val networkHelper: NetworkHelper // ✅ Move network check here
//) : ViewModel() {
//
//    open fun onEvent(action: T) {} // ✅ Only keeping the needed function
//
//    protected fun isNetworkAvailable(): Boolean {
//        return networkHelper.hasInternetConnection()
//    }
//
//    protected fun launchSafely(
//        dispatcher: CoroutineDispatcher = Dispatchers.IO, // ✅ Default to IO
//        block: suspend () -> Unit,
//        onError: (Exception) -> Unit = {} // ✅ Custom error handling
//    ) {
//        viewModelScope.launch( Dispatchers.IO) {
//            try {
//                withContext(dispatcher) { block() }
//            } catch (e: Exception) {
//                onError(e)
//            }
//        }
//    }
//}