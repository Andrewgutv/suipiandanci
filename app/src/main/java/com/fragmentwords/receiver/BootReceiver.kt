package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fragmentwords.utils.AlarmScheduler
import com.fragmentwords.utils.WorkManagerScheduler

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot completed, restoring schedules if needed")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                val prefs = context.getSharedPreferences("word_prefs", Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("notification_enabled", false)
                if (enabled) {
                    AlarmScheduler.schedulePeriodicAlarm(context)
                    WorkManagerScheduler.cancelRefresh(context)
                    Log.d(TAG, "Schedules restored after boot")
                }
            }
        }
    }
}
