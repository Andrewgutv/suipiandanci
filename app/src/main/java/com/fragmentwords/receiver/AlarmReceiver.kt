package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AlarmScheduler

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received, refreshing word notification")

        try {
            WordService.showNewWord(context)
            AlarmScheduler.rescheduleNextAlarm(context)
            Log.d(TAG, "Word notification refreshed and next alarm scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle alarm", e)
        }
    }
}
