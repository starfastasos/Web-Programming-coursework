package com.example.mysupermarketapplication.data

import android.util.Log // Import for Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException // Import for specific exception

/**
 * TypeConverters to enable Room to store [LocalizedText] as JSON strings.
 */
class Converters {
    private val gson = Gson()

    /**
     * Converts a [LocalizedText] object to a JSON string for database storage.
     * Handles potential exceptions during JSON serialization.
     */
    @TypeConverter
    fun fromLocalizedText(localizedText: LocalizedText?): String? {
        return try {
            localizedText?.let { gson.toJson(it) }
        } catch (e: Exception) {
            Log.e("Converters", "Error converting LocalizedText to JSON: ${e.message}", e)
            null // Return null on error, or an empty JSON string if preferred
        }
    }

    /**
     * Converts a JSON string back into a [LocalizedText] object.
     * Handles potential exceptions during JSON deserialization.
     */
    @TypeConverter
    fun toLocalizedText(json: String?): LocalizedText? {
        return try {
            json?.let { gson.fromJson(it, LocalizedText::class.java) }
        } catch (e: JsonSyntaxException) { // Catch specific JSON parsing errors
            Log.e("Converters", "JsonSyntaxException converting JSON to LocalizedText: ${e.message}. JSON: '$json'", e)
            null // Return null on parsing error
        } catch (e: Exception) { // Catch any other unexpected errors
            Log.e("Converters", "Error converting JSON to LocalizedText: ${e.message}. JSON: '$json'", e)
            null // Return null on unexpected error
        }
    }
}
