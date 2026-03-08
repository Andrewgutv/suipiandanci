package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fragmentwords.service.WordService

/**
 * 开机启动接收器 - 自动启动前台服务
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot completed, starting WordService")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                // 检查用户是否启用了通知功能
                val prefs = context.getSharedPreferences("word_prefs", Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("notification_enabled", true)

                if (enabled) {
                    // 启动前台服务
                    WordService.startService(context)
                    Log.d(TAG, "WordService started successfully")
                }
            }
        }
    }
}
