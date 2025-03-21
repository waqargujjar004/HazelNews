package com.hazelmobile.cores.extensions

import android.content.Intent
import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.hazelmobile.cores.utils.GenericCallback
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

fun String.capitalizeFirst(): String {
    if (this.isEmpty()) return ""
    val first = this[0]
    return if (Character.isUpperCase(first)) this
    else Character.toUpperCase(first).toString() + this.substring(1)
}

fun Int.checkDigit() = if (this <= 9) "0$this" else this.toString()

fun Int.divideToPercent(divideTo: Int): Int {
    return if (divideTo == 0) 0
    else ((this / divideTo.toFloat()) * 100).toInt()
}

fun Intent.isLaunchFromHistory(): Boolean =
    this.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY

fun String?.toHtml(): Spanned? {
    return when {
        this == null -> SpannableString("")

        isVersionGreaterThanEqualTo(Build.VERSION_CODES.N) -> Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)

        else -> Html.fromHtml(this)
    }
}

inline fun <T> ArrayList<T>?.isNotEmpty(callback: () -> Unit) {
    if (this.isNullOrEmpty().not()) callback()
}

inline fun <T> MutableList<T>?.isNotEmpty(callback: () -> Unit) {
    if (this.isNullOrEmpty().not()) callback()
}

fun <T> LiveData<T>.observeOnce(observer: (T) -> Unit) {
    observeForever(object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner, object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}

inline fun <reified T> ArrayList<T>.isLastIndex(index: Int): Boolean = index == lastIndex

inline fun <reified T> List<T>.isLastIndex(index: Int): Boolean = index == lastIndex

inline fun <reified T> List<T>.isValidIndex(index: Int): Boolean = index > -1 && index < size

inline fun <reified T> List<T>.isValidIndex(index: Int, callback: GenericCallback<T>) {
    if (index > -1 && index < size) callback(this[index])
}

inline fun <reified T> Array<T>.isValidIndex(index: Int): Boolean = index > -1 && index < size

inline fun <reified E> MutableList<E>.swap(index1: Int, index2: Int) {
    this[index1] = this[index2].also { this[index2] = this[index1] }
}

inline fun <reified T> Array<T>.swap(index1: Int, index2: Int) {
    this[index1] = this[index2].also { this[index2] = this[index1] }
}

inline fun <reified T> Array<T>.moveToFirst(index: Int): Array<T> {
    val item = this[index]
    val list = this.toMutableList()
    list.removeAt(index)
    list.add(0, item)
    return list.toTypedArray()
}

fun Long.toCalculateSeconds(): Long {
    return ((System.currentTimeMillis() - this + 500) / 1000L)
}


fun Long.toReadableSize(): String {
    try {
        val symbolsEnToUS: DecimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US)

        if (this <= 0) return "0KB"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat(
            "#,##0.#",
            symbolsEnToUS
        ).format(this / 1024.0.pow(digitGroups.toDouble()))
            .toString() + " " + units[digitGroups]
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return "0KB"

}

fun Long.toReadableDate(): String {
    try {
        var date = this
        date *= 1000L
        return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(date))
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return "12-12-2022"
}

fun formatString(format: String, vararg params: Any): String {
    return String.format(Locale.getDefault(), format, *params)
}
