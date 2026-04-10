package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.fragmentwords.service.WordService

class ScreenUnlockReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenUnlockReceiver"
        private const val PREFS_NAME = "word_prefs"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_LAST_REFRESH_TIME = "last_refresh_time"
        private const val KEY_JUST_CLICKED = "just_clicked_button"
        private const val KEY_CLICK_TIME = "button_click_time"
        private const val REFRESH_INTERVAL_MS = 5_000L
        private const val IMMEDIATE_REFRESH_WINDOW_MS = 3_000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_SCREEN_ON) {
            Log.d(TAG, "Screen on ignored; refresh only on unlock")
            return
        }
        if (action != Intent.ACTION_USER_PRESENT) {
            return
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, false)
        if (!enabled) {
            Log.d(TAG, "Notification push disabled, skip unlock refresh")
            return
        }

        refreshWordIfNeeded(context, prefs)
    }

    private fun refreshWordIfNeeded(context: Context, prefs: SharedPreferences) {
        val currentTime = System.currentTimeMillis()
        val lastRefreshTime = prefs.getLong(KEY_LAST_REFRESH_TIME, 0L)
        val justClicked = prefs.getBoolean(KEY_JUST_CLICKED, false)
        val clickTime = prefs.getLong(KEY_CLICK_TIME, 0L)
        val timeSinceClick = currentTime - clickTime

        if (justClicked && timeSinceClick in 0 until IMMEDIATE_REFRESH_WINDOW_MS) {
            Log.d(TAG, "Immediate refresh after notification action")
            refreshNow(context, prefs, currentTime)
            return
        }

        if (justClicked && timeSinceClick >= IMMEDIATE_REFRESH_WINDOW_MS) {
            prefs.edit().putBoolean(KEY_JUST_CLICKED, false).apply()
        }

        if (currentTime - lastRefreshTime < REFRESH_INTERVAL_MS) {
            Log.d(TAG, "Refresh skipped because interval has not elapsed")
            return
        }

        Log.d(TAG, "Refreshing word notification after unlock")
        refreshNow(context, prefs, currentTime)
    }

    private fun refreshNow(context: Context, prefs: SharedPreferences, currentTime: Long) {
        WordService.showNewWord(context)
        prefs.edit()
            .putLong(KEY_LAST_REFRESH_TIME, currentTime)
            .putBoolean(KEY_JUST_CLICKED, false)
            .apply()
    }
}
