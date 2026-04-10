package com.fragmentwords.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.fragmentwords.receiver.AlarmReceiver

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"
    private const val ALARM_REQUEST_CODE = 1001
    private const val DEFAULT_INTERVAL_MS = 30L * 60L * 1000L

    fun schedulePeriodicAlarm(context: Context, intervalMs: Long = DEFAULT_INTERVAL_MS): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(context)
        val triggerAtMillis = System.currentTimeMillis() + intervalMs

        val scheduled = tryScheduleExactOrFallback(
            alarmManager = alarmManager,
            triggerAtMillis = triggerAtMillis,
            intervalMs = intervalMs,
            pendingIntent = pendingIntent,
            exactMessage = "Scheduled exact alarm every ${intervalMs / 1000 / 60} minutes",
            fallbackMessage = "Exact alarm unavailable, scheduled inexact fallback"
        )

        if (scheduled) {
            context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
                .edit()
                .putLong("alarm_interval", intervalMs)
                .apply()
        }

        return scheduled
    }

    fun rescheduleNextAlarm(context: Context): Boolean {
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val intervalMs = prefs.getLong("alarm_interval", DEFAULT_INTERVAL_MS)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(context)
        val triggerAtMillis = System.currentTimeMillis() + intervalMs

        return tryScheduleExactOrFallback(
            alarmManager = alarmManager,
            triggerAtMillis = triggerAtMillis,
            intervalMs = intervalMs,
            pendingIntent = pendingIntent,
            exactMessage = "Rescheduled exact alarm in ${intervalMs / 1000 / 60} minutes",
            fallbackMessage = "Exact alarm unavailable, rescheduled with inexact fallback"
        )
    }

    fun cancelAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(createPendingIntent(context))
        Log.d(TAG, "Cancelled all alarms")
    }

    private fun tryScheduleExactOrFallback(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        intervalMs: Long,
        pendingIntent: PendingIntent,
        exactMessage: String,
        fallbackMessage: String
    ): Boolean {
        return try {
            scheduleExact(alarmManager, triggerAtMillis, pendingIntent)
            Log.d(TAG, exactMessage)
            true
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm denied by system, falling back", e)
            try {
                scheduleInexact(alarmManager, triggerAtMillis, intervalMs, pendingIntent)
                Log.w(TAG, fallbackMessage)
                true
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Failed to schedule fallback alarm", fallbackError)
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
            false
        }
    }

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleExact(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun scheduleInexact(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        intervalMs: Long,
        pendingIntent: PendingIntent
    ) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager.setWindow(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    intervalMs.coerceAtMost(5 * 60 * 1000L),
                    pendingIntent
                )
            }
            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }
}
