package com.hazelmobile.cores.bases.activity

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

open class LifeCycleRegister: DefaultLifecycleObserver {
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
    open fun onStarted() {}
    open fun onResumed() {}
    open fun onPaused() {}
    open fun onStopped() {}
}