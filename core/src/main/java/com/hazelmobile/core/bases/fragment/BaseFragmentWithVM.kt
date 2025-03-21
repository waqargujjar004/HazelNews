package com.hazelmobile.cores.bases.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.hazelmobile.cores.extensions.lazyAndroid

abstract class BaseFragmentWithVM<VM : ViewModel, Binding : ViewBinding>(
    private val viewModelClass: Class<VM>,
    bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding,
) : BaseFragment<Binding>(bindingInflater) {

    val viewModel : VM by lazyAndroid { ViewModelProvider(this)[viewModelClass] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}