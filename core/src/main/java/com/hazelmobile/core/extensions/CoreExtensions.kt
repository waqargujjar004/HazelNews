package com.hazelmobile.cores.extensions

import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.ChecksSdkIntAtLeast
import com.hazelmobile.cores.utils.GenericCallback
import com.hazelmobile.cores.utils.SimpleCallback
import kotlinx.coroutines.CoroutineExceptionHandler
import java.text.DecimalFormat
import kotlin.math.ceil

private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    println("CoroutineException $exception handled !")
}

fun <T> lazyAndroid(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

fun delay(milliseconds: Long = 500L, runnable: Runnable): Handler {
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(runnable, milliseconds)
    return handler
}

@ChecksSdkIntAtLeast(parameter = 0)
fun isVersionGreaterThanEqualTo(version: Int): Boolean {
    return Build.VERSION.SDK_INT >= version
}

@ChecksSdkIntAtLeast(parameter = 0)
fun isVersionLessThanEqualTo(version: Int): Boolean {
    return Build.VERSION.SDK_INT <= version
}

fun getMemoryWithUnit(mem: Long, applyCeil: Boolean = false): String {

    val twoDecimalForm = DecimalFormat("#.##")

    val memory = mem / 1000.0
    var mb: Double = memory / 1024.0
    var gb: Double = memory / 1048576.0
    var tb: Double = memory / 1073741824.0

    if (applyCeil) {
        mb = ceil(mb)
        gb = ceil(gb)
        tb = ceil(tb)
    }

    return when {
        tb > 1 -> twoDecimalForm.format(gb) + " TB"
        gb > 1 -> twoDecimalForm.format(gb) + " GB"
        mb > 1 -> twoDecimalForm.format(mb) + " MB"
        else -> twoDecimalForm.format(memory) + " KB"
    }
}

inline fun <T : Any> checkNotNull(value: T?, callback: GenericCallback<T>) {
    if (value != null) callback(value)
}

inline fun <T : Any> checkNull(value: T?, callback: SimpleCallback) {
    if (value == null) callback()
}

fun isLandscape(): Boolean {
    return Resources.getSystem().displayMetrics.widthPixels - Resources.getSystem().displayMetrics.heightPixels > 0
}





