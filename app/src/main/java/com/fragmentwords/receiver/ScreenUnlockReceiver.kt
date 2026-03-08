package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fragmentwords.service.WordService

/**
 * 屏幕/解锁监听器
 * 当用户点亮屏幕或解锁时，自动刷新单词卡片
 */
class ScreenUnlockReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenUnlockReceiver"
        private const val PREFS_NAME = "word_prefs"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_LAST_REFRESH_TIME = "last_refresh_time"
        private const val REFRESH_INTERVAL_MS = 5000L // 最小刷新间隔 5 秒，避免频繁刷新
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)

        // 检查用户是否开启了通知功能
        if (!enabled) {
            Log.d(TAG, "Notification is disabled, skip refresh")
            return
        }

        when (action) {
            Intent.ACTION_USER_PRESENT -> {
                // 用户解锁屏幕
                Log.d(TAG, "User unlocked screen")
                refreshWordIfNeeded(context, prefs)
            }
            Intent.ACTION_SCREEN_ON -> {
                // 屏幕点亮 - 暂时不做处理，避免频繁触发
                // 只在解锁时刷新，体验更好
                Log.d(TAG, "Screen turned on (ignored, will refresh on unlock)")
            }
        }
    }

    /**
     * 检查是否需要刷新单词（避免频繁刷新）
     */
    private fun refreshWordIfNeeded(context: Context, prefs: android.content.SharedPreferences) {
        val lastRefreshTime = prefs.getLong(KEY_LAST_REFRESH_TIME, 0)
        val currentTime = System.currentTimeMillis()

        // 如果距离上次刷新不足 5 秒，跳过
        if (currentTime - lastRefreshTime < REFRESH_INTERVAL_MS) {
            Log.d(TAG, "Refresh skipped: too soon")
            return
        }

        // 刷新单词
        Log.d(TAG, "Refreshing word notification")
        WordService.showNewWord(context)

        // 更新最后刷新时间
        prefs.edit().putLong(KEY_LAST_REFRESH_TIME, currentTime).apply()
    }
}
