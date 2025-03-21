package com.hazelmobile.cores.bases.dialog

import android.R
import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.hazelmobile.cores.extensions.isInternetConnected

abstract class BaseDialogFragment<Binding : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding
) : DialogFragment() {

    private var _binding: Binding? = null
    protected val binding get() = _binding

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

    open fun onBackPress() {
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                onBackPress()
                true
            } else {
                false
            }
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        isCancelable = false
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
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

//    override fun getTheme() = com.example.asset_reources.R.style.ThemeDialog

}