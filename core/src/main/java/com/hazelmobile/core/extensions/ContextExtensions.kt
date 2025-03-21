package com.hazelmobile.cores.extensions

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.Settings
import android.provider.Settings.Secure
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.hazelmobile.cores.utils.STORAGE_PERMISSION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.CoroutineContext

fun Activity.isActivityAlive(callback: (Activity) -> Unit) {
    try {
        if (isFinishing.not() &&
            isDestroyed.not()
        ) {
            callback(this)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.isActivityAlive():Boolean {
    return try {
        if (isFinishing.not() &&
            isDestroyed.not()
        ) {
            return true
        }else false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun Fragment.isAlive(callback: (Activity) -> Unit) {
    if (activity != null && isAdded && !isDetached) {
        activity?.let { it.isActivityAlive { activity -> callback(activity) } }
    }
}

fun Fragment.isAlive():Boolean {
    return if (activity != null && isAdded && !isDetached) {
        val isLive = activity?.isActivityAlive()
        isLive?:false
    }else false
}

fun Context.getClassByName(className: String): Class<*>? {
    return javaClass.classLoader?.loadClass(className)
}

inline fun Activity.issActivityAlive(callback: () -> Unit) {
    try {
        if (isFinishing.not() && isDestroyed.not()) {
            callback()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

inline fun Activity.isAlive(callback: (Activity) -> Unit) {
    try {
        if (isFinishing.not() && isDestroyed.not()) callback(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

inline fun Context.isAlive(callback: (AppCompatActivity) -> Unit) {
    try {
        with(this as AppCompatActivity) {
            if (isFinishing.not() && isDestroyed.not()) callback(this)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.arePermissionsGranted(): Boolean {
    return when {
        isVersionGreaterThanEqualTo(Build.VERSION_CODES.R) -> Environment.isExternalStorageManager()
        isVersionGreaterThanEqualTo(Build.VERSION_CODES.M) -> checkSelfPermission(STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED
        else -> true
    }
}

fun Context.checkNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.let { context ->
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun Activity.getNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            999
        )
    }
}

fun Activity.checkPermissionRationale(permission: String = STORAGE_PERMISSION): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        shouldShowRequestPermissionRationale(permission)
    }
    else {
        false
    }
}

fun Context.getSettingsIntent(): Intent {
    return if (isVersionGreaterThanEqualTo(Build.VERSION_CODES.R)) {
        try {
            val permission = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            val intent = Intent(permission)
            intent.data = Uri.parse("package:$packageName")
            intent
        } catch (e: ActivityNotFoundException) {
            val intent = Intent()
            try {
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            } catch (_: ActivityNotFoundException) {
            } catch (_: Exception) {
            }
            intent
        } catch (e: Exception) {
            Intent()
        }
    } else {
        try {
            val dialogIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            dialogIntent.data = uri
            dialogIntent
        } catch (e: WindowManager.BadTokenException) {
            e.printStackTrace()
            Intent()
        } catch (e: Exception) {
            e.printStackTrace()
            Intent()
        }
    }
}

fun Activity.isSystemThemeDark(): Boolean {
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    }
}

fun Activity.setSplashStatusBarColor(){
// Make the status bar transparent
    window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
    window.statusBarColor = Color.TRANSPARENT
}

fun Context.getWhatsappPackage(): String {
    return if (isPackageInstalled("com.whatsapp")) "com.whatsapp"
    else if (isPackageInstalled("com.whatsapp.w4b")) "com.whatsapp.w4b"
    else if (isPackageInstalled("com.gbwhatsapp")) "com.gbwhatsap"
    else if (isPackageInstalled("com.lbe.parallel.intl")) "com.lbe.parallel.intl"
    else ""
}

fun Context.getFacebookPackage(): String {
    return if (isPackageInstalled("com.facebook.katana")) "com.facebook.katana"
    else if (isPackageInstalled("com.facebook.lite")) "com.facebook.lite"
    else if (isPackageInstalled("com.facebook.orca")) "com.facebook.orca"
    else if (isPackageInstalled("com.facebook.mlite")) "com.facebook.mlite"
    else ""
}

fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.getScreenWidthHeight(): Pair<Int, Int> {
    return Pair(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
}

fun Activity.getActionBarHeight(): Int {
    val typedValue = TypedValue()
    var actionBarHeight = 0
    if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
        actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
    }
    return actionBarHeight
}

fun Context.getMemoryInfo(): String {
    return try {
        val memoryInfo = ActivityManager.MemoryInfo()
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memoryInfo)
        val totalMemory = memoryInfo.totalMem
        val availMem = memoryInfo.availMem
        String.format(
            Locale.getDefault(), "%s/%s",
            getMemoryWithUnit(availMem),
            getMemoryWithUnit(totalMemory, true)
        )
    } catch (e: Exception) {
        ""
    }
}

suspend fun withIO(block: suspend CoroutineScope.() -> Unit) {
    withContext(context = IO, block = block)
}

suspend fun withMain(block: suspend CoroutineScope.() -> Unit) {
    withContext(context = Main, block = block)
}

fun AppCompatActivity.launchDefault(
    context: CoroutineContext = Dispatchers.Default,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return lifecycleScope.launch(context = context, block = block)
}

fun AppCompatActivity.launchMain(job: Job? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return launchDefault(context = Main + (job ?: Job()), block = block)
}

fun AppCompatActivity.launchIO(job: Job? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return launchDefault(IO + (job ?: Job()), block = block)
}

fun Fragment.launchDefault(
    context: CoroutineContext = Dispatchers.Default,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return lifecycleScope.launch(context = context, block = block)
}

fun Fragment.launchMain(job: Job? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return launchDefault(context = Main + (job ?: Job()), block = block)
}

fun Fragment.launchIO(job: Job? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return launchDefault(context = IO + (job ?: Job()), block = block)
}

fun ViewModel.launchDefault(
    context: CoroutineContext = Dispatchers.Default,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return viewModelScope.launch(context = context, block = block)
}

fun ViewModel.launchMain(job: Job? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return launchDefault(context = Main + (job ?: Job()), block = block)
}

fun ViewModel.launchIO(job: Job? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return launchDefault(context = IO + (job ?: Job()), block = block)
}

fun Context.isDarkModeEnabled(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun Context.showDatePicker(date: String = "", callback: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    var year = calendar.get(Calendar.YEAR)
    var month = calendar.get(Calendar.MONTH)
    var day = calendar.get(Calendar.DAY_OF_MONTH)

    try {
        if (date.isNotEmpty()) {
            date.split("-").let {
                day = it[0].toInt()
                month = it[1].toInt() - 1
                year = it[2].toInt()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val datePickerDialog = DatePickerDialog(this, { _, selectedYear, monthOfYear, dayOfMonth ->

        val selectedCalendar = Calendar.getInstance().apply {
            set(selectedYear, monthOfYear, dayOfMonth)
        }

        // Format the date according to the current locale
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedCalendar.time)

        callback(formattedDate)

//        callback("${dayOfMonth.checkDigit()}-${(monthOfYear + 1).checkDigit()}-$selectedYear")
    }, year, month, day)

    datePickerDialog.show()
}

fun Context.getUniqueDeviceId(): String {
    return Secure.getString(contentResolver, Secure.ANDROID_ID) ?: ""
}

fun Context.isMyLauncherDefault(): Boolean {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.type = "application/pdf"
    val res = packageManager.resolveActivity(intent, 0)
    return res?.activityInfo != null && res.activityInfo?.packageName == packageName
}

fun Context.shouldShowDefaultDialog(): Boolean {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.type = "application/pdf"
    val res = packageManager.resolveActivity(intent, 0)
    return res?.activityInfo == null ||
            res.activityInfo?.packageName == "android"
}

fun Activity.isAppCompact(): AppCompatActivity? {
    return this as? AppCompatActivity
}

fun PopupWindow.dimBehind() {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = 0.5f
    wm.updateViewLayout(container, p)
}

fun Context.isInternetConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    // For 29 api or above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    // For below 29 api
    else {
        if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true) {
            return true
        }
    }
    return false
}

inline fun <reified T : Any> Activity.openActivity(
    finish: Boolean = false,
    vararg params: Pair<String, Any?>
) {
    val intent = Intent(this, T::class.java)
    if (params.isNotEmpty()) fillIntentArguments(intent, params)
    startActivity(intent)
    if (finish) finish()
//    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}


fun fillIntentArguments(intent: Intent, params: Array<out Pair<String, Any?>>) {
    params.forEach {
        when (val value = it.second) {
            is Int -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is CharSequence -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Char -> intent.putExtra(it.first, value)
            is Short -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            is Parcelable -> intent.putExtra(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                else -> {}
            }
            is IntArray -> intent.putExtra(it.first, value)
            is LongArray -> intent.putExtra(it.first, value)
            is FloatArray -> intent.putExtra(it.first, value)
            is DoubleArray -> intent.putExtra(it.first, value)
            is CharArray -> intent.putExtra(it.first, value)
            is ShortArray -> intent.putExtra(it.first, value)
            is BooleanArray -> intent.putExtra(it.first, value)
            else -> {}
        }
        return@forEach
    }
}

inline fun <reified T : Any> Context.openActivity(
    vararg params: Pair<String, Any?>
) {
    val intent = Intent(this, T::class.java)
    if (params.isNotEmpty()) fillIntentArguments(intent, params)
    startActivity(intent)
    if (this is Activity) animateActivity(false)
}

fun Activity.animateActivity(
    isClosing: Boolean = true,
    startAnimation: Int = android.R.anim.slide_in_left,
    endAnimation: Int = android.R.anim.slide_out_right,
) {
    if (isVersionLessThanEqualTo(Build.VERSION_CODES.S_V2)) {
        if (isClosing.not()) {
            overridePendingTransition(startAnimation, endAnimation)
        } else {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }
}


inline fun <reified T : Any> Context.openActivity(finish: Boolean = false, vararg params: Pair<String, Any?>) {
    if (finish) if (this is Activity) finish()
    openActivity<T>(params = params)
}

inline fun <reified T : Any> Context.getActivityIntent(vararg params: Pair<String, Any?>): Intent {
    val intent = Intent(this, T::class.java)
    if (params.isNotEmpty()) fillIntentArguments(intent, params)
    return intent
}

inline fun <reified T : Any> Context.getBroadcastIntent(vararg params: Pair<String, Any?>): Intent {
    return getActivityIntent<T>(params = params)
}

inline fun <reified T : Any> AppCompatActivity.findFragment(fragmentTag: String): T? {
    val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
    return fragment as? T
}

inline fun <reified T : Any> Fragment.findFragment(fragmentTag: String): T? {
    val fragment = childFragmentManager.findFragmentByTag(fragmentTag)
    return fragment as? T
}

fun Context.scanMedia(path: String = Environment.getExternalStorageDirectory().absolutePath, callback: MediaScannerConnection.OnScanCompletedListener? = null) {
    CoroutineScope(IO).launch {
        MediaScannerConnection.scanFile(
            this@scanMedia,
            arrayOf(path),
            null,
            callback
        )
    }
}

fun AppCompatActivity.screenBrightness(newBrightnessValue: Double) {
    /*
     * WindowManager.LayoutParams settings = getWindow().getAttributes();
     * settings.screenBrightness = newBrightnessValue;
     * getWindow().setAttributes(settings);
     */
    val lp: WindowManager.LayoutParams = window.attributes
    val newBrightness = newBrightnessValue.toFloat()
    lp.screenBrightness = newBrightness / 255f
    window.attributes = lp
}

@RequiresApi(Build.VERSION_CODES.M)
fun Context.getBrightness(default: Int = 100): Int {
    var brightness = -1
    if (Settings.System.canWrite(this)) {
        Settings.System.putInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        )
        brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1)
    }
    return brightness.coerceAtLeast(default)
}







//fun Activity.getIntentBoolean(key: IntentKeys, defaultValue: Boolean = false): Boolean {
//    return intent?.getBooleanExtra(key.toString(), defaultValue) ?: defaultValue
//}
//
//fun Activity.getIntentString(key: IntentKeys, defaultValue: String = ""): String {
//    return intent?.getStringExtra(key.toString()) ?: defaultValue
//}
//
//fun Activity.getIntentFloat(key: IntentKeys, defaultValue: Float = 0f): Float {
//    return intent?.getFloatExtra(key.toString(), defaultValue) ?: defaultValue
//}
//
//fun Activity.getIntentInt(key: IntentKeys, defaultValue: Int = 0): Int {
//    return intent?.getIntExtra(key.toString(), defaultValue) ?: defaultValue
//}
//
//fun Activity.getIntentLong(key: IntentKeys, defaultValue: Long = 0L): Long {
//    return intent?.getLongExtra(key.toString(), defaultValue) ?: defaultValue
//}
