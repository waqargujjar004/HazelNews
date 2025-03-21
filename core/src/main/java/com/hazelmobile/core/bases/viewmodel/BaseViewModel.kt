package com.hazelmobile.cores.bases.viewmodel

import androidx.lifecycle.ViewModel

abstract class BaseViewModel<T: Any, Model: Any> : ViewModel() {
    open fun onEvent(action: T) {}
    open fun onEvent(action: T, model: Model) {}

}

