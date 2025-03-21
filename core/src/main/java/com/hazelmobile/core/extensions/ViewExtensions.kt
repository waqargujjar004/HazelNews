package com.hazelmobile.cores.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.hazelmobile.cores.utils.GenericCallback
import com.hazelmobile.cores.utils.GenericPairCallback

fun EditText.moveCursorToEnd() = setSelection(text.length)

fun EditText.textString(): String = text.toString()

fun TextView.textString(): String = text.toString()

fun View.hideKeyboard() {
    try {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        isFocusableInTouchMode = false
        this.clearFocus()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun View.showKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    requestFocus()
    inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun CardView.backgroundColor(@ColorRes colorId: Int) {
    setCardBackgroundColor(ContextCompat.getColor(context, colorId))
}

fun View.backgroundColor(@ColorRes colorId: Int) {
    setBackgroundColor(ContextCompat.getColor(context, colorId))
}

fun View.backgroundDrawable(@DrawableRes drawableId: Int) {
    background = ContextCompat.getDrawable(context, drawableId)
}

fun TextView.textColor(colorId: Int) {
    try {
        setTextColor(ContextCompat.getColor(context, colorId))
    } catch (e: Exception) {
        setTextColor(colorId)
    }
}

fun ImageView.colorFilter(@ColorRes colorId: Int) {
    try {
        setColorFilter(ContextCompat.getColor(context, colorId))
    } catch (e: Exception) {
        setColorFilter(colorId)
    }
}

fun CheckBox.colorFilter(@ColorRes colorId: Int) {
    try {
        setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorId)))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Bitmap.rotate(degrees: Float = 0f, shouldFlip: Boolean = true): Bitmap {
    val matrix = Matrix().apply {
        postRotate(degrees)
        if (shouldFlip) postScale(-1f, 1f, width / 2f, width / 2f)
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Group.setAllOnClickListener(listener: OnClickListener?) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}

fun Group.setSmartAlpha(alpha: Float) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).alpha = alpha
    }
}

fun View.setSmartClickListener(callback: GenericCallback<View>) {
    this.setOnClickListener {
        isEnabled = false
        callback(this)
        delay(300) { isEnabled = true }
    }
}

@SuppressLint("ClickableViewAccessibility")
fun View.setSmartTouchListener(time: Long = 700, callback: GenericPairCallback<View, MotionEvent>) {
    this.setOnTouchListener { view, motionEvent ->
        isEnabled = false
        callback(view, motionEvent)
        delay(time) { isEnabled = true }
        true // Indicates that the event was handled
    }
}

fun View.setSmartClickListener(time: Long = 500, listener: OnClickListener) {
    this.setOnClickListener {
        isEnabled = false
        listener.onClick(it)
        delay(time) { isEnabled = true }
    }
}

fun Window.hideKeyboard() {
    try {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun Window.showKeyboard() {
    try {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
