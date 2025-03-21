package com.hazelmobile.cores.bases.bottomsheet

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.hazelmobile.cores.extensions.getScreenWidthHeight
import com.hazelmobile.cores.utils.LocaleHelper
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

abstract class BaseBottomSheetDialogFragment<Binding : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding
) : BottomSheetDialogFragment() {

    private var _binding: Binding? = null
    val binding get() = _binding

    open var bottomSheetHeight: Float = 0.6f

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

    private val bottomSheetListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            view?.let {
                it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog
                val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
                bottomSheet?.let { sheet ->
                    val behavior = BottomSheetBehavior.from(sheet)
                    behavior.maxHeight = (sheet.context.getScreenWidthHeight().second * bottomSheetHeight).toInt()
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        view?.viewTreeObserver?.addOnGlobalLayoutListener(bottomSheetListener)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        context?.let {
            val language = LocaleHelper.getLanguage(it)
            val locale = Locale(language)
            Locale.setDefault(locale)
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
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
            try {
                onViewBindingCreated(savedInstanceState)

                binding?.run {
                    bindViews()
                    bindListeners()
                    bindObservers()

                    loadData()
                    loadAds()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    override fun onDestroyView() {
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(bottomSheetListener)
        super.onDestroyView()
    }

}
