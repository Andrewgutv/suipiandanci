package com.fragmentwords.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AppPreferences
import com.fragmentwords.utils.NotificationPermissionHelper

/**
 * 单词自动刷新 Worker
 * 定期刷新通知栏的单词
 */
class WordRefreshWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        private const val TAG = "WordRefreshWorker"
        const val WORK_NAME = "word_refresh_work"
    }

    override fun doWork(): Result {
        Log.d(TAG, "Refreshing word notification")

        if (!AppPreferences.isNotificationEnabled(applicationContext)) {
            Log.d(TAG, "Push disabled, skipping worker refresh")
            return Result.success()
        }

        if (!NotificationPermissionHelper.canPostNotifications(applicationContext)) {
            Log.d(TAG, "Notification access unavailable, skipping worker refresh")
            return Result.success()
        }

        try {
            // 触发显示新单词
            WordService.showNewWord(applicationContext)
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh word", e)
            return Result.failure()
        }
    }
}
