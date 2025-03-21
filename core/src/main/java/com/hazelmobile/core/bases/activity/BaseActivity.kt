package com.hazelmobile.core.bases.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.hazelmobile.cores.bases.activity.LifeCycleRegister
import com.hazelmobile.cores.extensions.animateActivity
import com.hazelmobile.cores.extensions.isInternetConnected
import com.hazelmobile.cores.extensions.lazyAndroid
import com.hazelmobile.cores.utils.LocaleHelper

abstract class BaseActivity<Binding : ViewBinding>(private val bindingFactory: (LayoutInflater) -> Binding) :
    AppCompatActivity() {

    val binding: Binding by lazyAndroid { bindingFactory(layoutInflater) }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = onBackPress()
    }

    open fun onPreViewBindingCreated(savedInstanceState: Bundle?) {}
    open fun onViewBindingCreated(savedInstanceState: Bundle?) {}

//    open fun onStarted() {}
//    open fun onResumed() {}
//    open fun onPaused() {}
//    open fun onStopped() {}

    open fun Binding.bindViews() {}
    open fun Binding.bindListeners() {}
    open fun Binding.bindObservers() {}

    open fun Binding.loadData() {}
    open fun Binding.loadAds() {}


    open fun onBackPress() {
        finish()
        animateActivity(isClosing = true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.onAttach(this)

        onBackPressedDispatcher.addCallback(backPressedCallback)
        lifecycle.addObserver(LifeCycleRegister())

/*        lifecycle.addObserver(object : DefaultLifecycleObserver {
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
        })*/

        with(binding) {

            onPreViewBindingCreated(savedInstanceState)

            setContentView(root)

            onViewBindingCreated(savedInstanceState)

            bindViews()
            bindListeners()
            bindObservers()

            loadData()

            if (isInternetConnected()) loadAds()

        }

    }

}