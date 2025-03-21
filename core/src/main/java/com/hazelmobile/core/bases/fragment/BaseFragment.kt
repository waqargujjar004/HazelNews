package com.hazelmobile.cores.bases.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.hazelmobile.cores.extensions.isInternetConnected

abstract class BaseFragment<Binding : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding,
) : Fragment() {

    private var _binding: Binding? = null
    val binding get() = _binding

    open fun onViewBindingCreated(savedInstanceState: Bundle?) {}

    open fun onStarted() {}
    open fun onResumed() {}
    open fun onPaused() {}
    open fun onStopped() {}

    open fun Binding.bindViews() {}
    open fun Binding.bindListeners() {}
    open fun Binding.bindObservers() {}

    open fun Binding.loadData() {}
    open fun Binding.loadAds() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                onStarted()
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onResumed()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                onPaused()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                onStopped()
            }
        })

        view.post {
            onViewBindingCreated(savedInstanceState)
            binding?.run {

                bindViews()
                bindListeners()
                bindObservers()

                loadData()

                if (root.context.isInternetConnected()) loadAds()

            }
        }
    }

}