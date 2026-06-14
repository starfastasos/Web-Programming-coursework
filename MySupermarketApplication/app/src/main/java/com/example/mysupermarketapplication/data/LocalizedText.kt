package com.example.mysupermarketapplication.data

import android.content.Context
import android.os.Build
import com.google.gson.annotations.SerializedName
import java.util.Locale

/**
 * Holds text in multiple languages, currently Greek and English.
 * Supports returning the text matching the device's current locale.
 */
data class LocalizedText(
    @SerializedName("el") val greek: String,
    @SerializedName("en") val english: String
) {
    /**
     * Returns the localized string based on the device's language.
     * Defaults to English if the locale is not Greek.
     */
    fun getLocalized(context: Context): String {
        val currentLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        return if (currentLocale.language == "el") greek else english
    }
}
