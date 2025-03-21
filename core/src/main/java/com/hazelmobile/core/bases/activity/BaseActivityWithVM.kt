package com.hazelmobile.cores.bases.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.hazelmobile.core.bases.activity.BaseActivity
import com.hazelmobile.cores.extensions.lazyAndroid

abstract class BaseActivityWithVM<VM: ViewModel, Binding : ViewBinding>(
    private val viewModelClass: Class<VM>,
    bindingFactory: (LayoutInflater) -> Binding
) : BaseActivity<Binding>(bindingFactory) {

    val viewModel : VM by lazyAndroid { ViewModelProvider(this)[viewModelClass] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}