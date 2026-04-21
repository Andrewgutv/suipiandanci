package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fragmentwords.data.WordRepository
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AppPreferences
import com.fragmentwords.utils.AlarmScheduler
import com.fragmentwords.utils.NotificationPermissionHelper

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received, refreshing word notification")

        if (!AppPreferences.isNotificationEnabled(context)) {
            AlarmScheduler.cancelAlarms(context)
            WordRepository(context).clearCurrentWord()
            AppPreferences.clearNotificationRuntimeState(context)
            Log.d(TAG, "Push disabled, skipping alarm refresh")
            return
        }

        if (!NotificationPermissionHelper.canPostNotifications(context)) {
            Log.d(TAG, "Notification access unavailable, skipping alarm refresh")
            return
        }

        try {
            WordService.showNewWord(context)
            AlarmScheduler.rescheduleNextAlarm(context)
            Log.d(TAG, "Word notification refreshed and next alarm scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle alarm", e)
        }
    }
}
