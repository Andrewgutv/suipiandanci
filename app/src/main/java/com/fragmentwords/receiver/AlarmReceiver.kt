package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AlarmScheduler

/**
 * 闹钟接收器
 * 接收 AlarmManager 的定时广播，触发单词推送
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received, showing word notification")

        try {
            // 显示新单词
            WordService.showNewWord(context)

            // 重新调度下一次闹钟（形成循环）
            AlarmScheduler.rescheduleNextAlarm(context)

            Log.d(TAG, "Word notification shown and next alarm scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle alarm", e)
        }
    }
}
