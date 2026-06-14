package com.example.mysupermarketapplication.util

import java.text.Normalizer

// Precompiled regex to match Unicode combining diacritical marks, used for removing accents.
private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

/**
 * Removes diacritical marks (accents) from the string, converting characters like 'é' to 'e'.
 * This is particularly useful for features like accent-insensitive search or sorting.
 *
 * For example, "café" would become "cafe".
 *
 * @return A new string with all diacritics removed.
 */
fun String.unaccent(): String {
    val normalizedString = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(normalizedString, "")
}
