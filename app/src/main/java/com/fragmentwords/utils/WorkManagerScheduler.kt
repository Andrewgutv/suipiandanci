package com.fragmentwords.utils

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fragmentwords.worker.WordRefreshWorker
import java.util.concurrent.TimeUnit

/**
 * WorkManager 调度器
 * 定期刷新单词
 */
object WorkManagerScheduler {

    private const val TAG = "WorkManagerScheduler"
    private const val DEFAULT_INTERVAL_MINUTES = 15L // 默认每15分钟刷新一次（WorkManager最小间隔）

    /**
     * 启动定期刷新
     */
    fun scheduleRefresh(context: Context, intervalMinutes: Long = DEFAULT_INTERVAL_MINUTES) {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false) // 不需要充电
            .setRequiresDeviceIdle(false) // 不需要设备空闲
            .build()

        val refreshRequest = PeriodicWorkRequestBuilder<WordRefreshWorker>(
            intervalMinutes, TimeUnit.MINUTES // 最小间隔 15 分钟
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WordRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // 替换已有的任务
            refreshRequest
        )

        Log.d(TAG, "Scheduled word refresh every $intervalMinutes minutes")
    }

    /**
     * 取消定期刷新
     */
    fun cancelRefresh(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WordRefreshWorker.WORK_NAME)
        Log.d(TAG, "Cancelled word refresh")
    }

    /**
     * 检查是否已调度
     */
    fun isScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WordRefreshWorker.WORK_NAME)
        return try {
            workInfos.get().isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
