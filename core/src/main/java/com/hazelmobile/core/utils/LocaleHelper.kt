package com.hazelmobile.cores.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Locale

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    fun onAttach(context: Context): Context {
        return onAttach(context, Locale.getDefault().language)
    }

    private fun onAttach(context: Context, defaultLanguage: String): Context {
        val lang = getPersistedData(context, defaultLanguage)
        return context.setLocale(lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(context, Locale.getDefault().language)
    }

    fun getUserCountry(context: Context): String {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountry = tm.simCountryIso

            if (simCountry != null && simCountry.length == 2) {
                return simCountry.lowercase()
            } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) {
                val networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) {
                    return networkCountry.lowercase()
                }
            }

        } catch (e: Exception) {
            return "unknown"
        }
        var countryCode = context.resources.configuration.locale.country
        if (countryCode.isBlank()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (context.resources.configuration.locales.size() > 0) {
                    countryCode = context.resources.configuration.locales[0].country
                }
            }
        }
        return countryCode
    }

    fun Context.setLocale(language: String): Context {
        persist(this, language)
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) updateResources(language)
        else updateResourcesLegacy(language)
    }

//    private fun getPersistedData(context: Context, defaultLanguage: String): String {
//        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
//        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage) ?: Locale.getDefault().language
//    }
private fun getPersistedData(context: Context, defaultLanguage: String): String {
    return runBlocking { // Use coroutines to avoid main-thread disk read violation
        withContext(Dispatchers.IO) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.getString(SELECTED_LANGUAGE, defaultLanguage) ?: Locale.getDefault().language
        }
    }
}



    private fun persist(context: Context, language: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun Context.updateResources(language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return createConfigurationContext(configuration)
    }

    private fun Context.updateResourcesLegacy(language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = resources
        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return this
    }
}
