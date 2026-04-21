package com.fragmentwords.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppPreferences {

    private const val PREFS_NAME = "word_prefs"
    private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    private const val KEY_SELECTED_LIBRARIES = "selected_libraries"

    private const val KEY_LAST_REFRESH_TIME = "last_refresh_time"
    private const val KEY_JUST_CLICKED = "just_clicked_button"
    private const val KEY_CLICK_TIME = "button_click_time"

    private val gson = Gson()

    fun wordPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isNotificationEnabled(context: Context): Boolean {
        return wordPrefs(context).getBoolean(KEY_NOTIFICATION_ENABLED, false)
    }

    fun setNotificationEnabled(context: Context, enabled: Boolean) {
        wordPrefs(context).edit()
            .putBoolean(KEY_NOTIFICATION_ENABLED, enabled)
            .apply()
    }

    fun getSelectedLibraries(context: Context): List<String> {
        val storedValue = wordPrefs(context).all[KEY_SELECTED_LIBRARIES]
        val libraries = when (storedValue) {
            is String -> parseLibraries(storedValue)
            is Set<*> -> LibrarySelection.normalize(storedValue.filterIsInstance<String>())
            else -> emptyList()
        }

        val effectiveLibraries = if (libraries.isEmpty()) {
            LibrarySelection.DEFAULT_SELECTED_LIBRARIES
        } else {
            libraries
        }

        saveSelectedLibraries(context, effectiveLibraries)
        return effectiveLibraries
    }

    fun saveSelectedLibraries(context: Context, libraries: Collection<String>) {
        val normalized = LibrarySelection.normalize(libraries).ifEmpty {
            LibrarySelection.DEFAULT_SELECTED_LIBRARIES
        }
        wordPrefs(context).edit()
            .putString(KEY_SELECTED_LIBRARIES, gson.toJson(normalized))
            .apply()
    }

    fun clearNotificationRuntimeState(context: Context) {
        wordPrefs(context).edit()
            .remove(KEY_LAST_REFRESH_TIME)
            .remove(KEY_JUST_CLICKED)
            .remove(KEY_CLICK_TIME)
            .apply()
    }

    private fun parseLibraries(rawValue: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            val parsed = gson.fromJson<List<String>>(rawValue, type).orEmpty()
            LibrarySelection.normalize(parsed)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
