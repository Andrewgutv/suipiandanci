package com.fragmentwords.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.fragmentwords.receiver.AlarmReceiver

/**
 * AlarmManager 调度器
 * 使用精确闹钟定期推送单词（即使App被杀死也能工作）
 */
object AlarmScheduler {

    private const val TAG = "AlarmScheduler"
    private const val ALARM_REQUEST_CODE = 1001

    /**
     * 定期推送间隔（毫秒）
     * 默认30分钟推送一次
     */
    private const val DEFAULT_INTERVAL_MS = 30L * 60L * 1000L

    /**
     * 启动定期闹钟
     * @param context 上下文
     * @param intervalMs 推送间隔（毫秒），最小30分钟
     */
    fun schedulePeriodicAlarm(context: Context, intervalMs: Long = DEFAULT_INTERVAL_MS) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 计算第一次触发时间（当前时间 + 间隔）
        val triggerAtMillis = System.currentTimeMillis() + intervalMs

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+ 使用 setExactAndAllowWhileIdle
                // 即使在低电模式（Doze）也能唤醒
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Android 4.4+ 使用 setExact
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // 旧版本使用 setRepeating
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    intervalMs,
                    pendingIntent
                )
            }

            // 保存间隔时间到 SharedPreferences
            val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("alarm_interval", intervalMs).apply()

            Log.d(TAG, "Scheduled periodic alarm every ${intervalMs / 1000 / 60} minutes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    /**
     * 重新调度下一次闹钟（在当前闹钟触发后调用）
     */
    fun rescheduleNextAlarm(context: Context) {
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val intervalMs = prefs.getLong("alarm_interval", DEFAULT_INTERVAL_MS)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + intervalMs

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }

            Log.d(TAG, "Rescheduled next alarm in ${intervalMs / 1000 / 60} minutes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reschedule alarm", e)
        }
    }

    /**
     * 取消所有闹钟
     */
    fun cancelAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled all alarms")
    }
}
