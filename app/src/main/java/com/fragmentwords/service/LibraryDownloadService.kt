package com.fragmentwords.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fragmentwords.R
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 词库下载服务
 *
 * 功能：
 * 1. 下载词库文件
 * 2. 显示下载进度
 * 3. 支持暂停/取消
 * 4. 下载完成后自动导入
 */
class LibraryDownloadService : Service() {

    companion object {
        private const val TAG = "LibraryDownloadService"
        const val CHANNEL_ID = "library_download_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_DOWNLOAD = "com.fragmentwords.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.fragmentwords.CANCEL_DOWNLOAD"
        const val ACTION_PAUSE_DOWNLOAD = "com.fragmentwords.PAUSE_DOWNLOAD"

        const val EXTRA_LIBRARY_ID = "library_id"
        const val EXTRA_LIBRARY_NAME = "library_name"
        const val EXTRA_DOWNLOAD_URL = "download_url"

        const val DOWNLOAD_STATUS_PROGRESS = "download_progress"
        const val DOWNLOAD_STATUS_SUCCESS = "download_success"
        const val DOWNLOAD_STATUS_ERROR = "download_error"
        const val DOWNLOAD_STATUS_CANCELLED = "download_cancelled"

        private var currentJob: Job? = null

        fun startDownload(context: Context, libraryId: String, libraryName: String, downloadUrl: String) {
            val intent = Intent(context, LibraryDownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_LIBRARY_ID, libraryId)
                putExtra(EXTRA_LIBRARY_NAME, libraryName)
                putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun cancelDownload(context: Context) {
            val intent = Intent(context, LibraryDownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var notificationManager: NotificationManager
    private var isDownloading = false
    private var isPaused = false

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): LibraryDownloadService = this@LibraryDownloadService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LibraryDownloadService onCreate")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LibraryDownloadService onStartCommand, action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val libraryId = intent.getStringExtra(EXTRA_LIBRARY_ID) ?: return START_NOT_STICKY
                val libraryName = intent.getStringExtra(EXTRA_LIBRARY_NAME) ?: return START_NOT_STICKY
                val downloadUrl = intent.getStringExtra(EXTRA_DOWNLOAD_URL) ?: return START_NOT_STICKY

                startDownload(libraryId, libraryName, downloadUrl)
            }
            ACTION_CANCEL_DOWNLOAD -> {
                cancelDownload()
            }
            ACTION_PAUSE_DOWNLOAD -> {
                pauseDownload()
            }
        }

        return START_NOT_STICKY
    }

    private fun startDownload(libraryId: String, libraryName: String, downloadUrl: String) {
        if (isDownloading) {
            Log.w(TAG, "Already downloading")
            return
        }

        isDownloading = true
        isPaused = false

        // 启动前台服务
        val notification = createProgressNotification(libraryName, 0, 0L, 100L)
        startForeground(NOTIFICATION_ID, notification)

        currentJob = serviceScope.launch {
            try {
                downloadLibrary(libraryId, libraryName, downloadUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                notifyError(libraryName, e.message)
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private suspend fun downloadLibrary(libraryId: String, libraryName: String, downloadUrl: String) {
        withContext(Dispatchers.IO) {
            // 如果是asset://开头的URL，从assets复制
            if (downloadUrl.startsWith("asset://")) {
                copyFromAssets(libraryId, libraryName, downloadUrl.removePrefix("asset://"))
            } else {
                // 从网络下载
                downloadFromNetwork(libraryId, libraryName, downloadUrl)
            }
        }
    }

    private suspend fun copyFromAssets(libraryId: String, libraryName: String, assetPath: String) {
        withContext(Dispatchers.IO) {
            try {
                val assetFileName = "data/${assetPath.removePrefix("data/")}"
                val inputStream = assets.open(assetFileName)
                val libraryDir = File(filesDir, "vocabularies")
                if (!libraryDir.exists()) {
                    libraryDir.mkdirs()
                }
                val outputFile = File(libraryDir, assetPath)

                val totalBytes = inputStream.available().toLong()
                var downloadedBytes = 0L
                val buffer = ByteArray(8192)
                var bytes: Int

                inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        while (input.read(buffer).also { bytes = it } != -1) {
                            if (isPaused) {
                                while (isPaused) {
                                    delay(100)
                                }
                            }

                            output.write(buffer, 0, bytes)
                            downloadedBytes += bytes

                            // 更新进度
                            val progress = (downloadedBytes * 100 / totalBytes).toInt()
                            updateNotificationProgress(libraryName, progress, downloadedBytes, totalBytes)
                        }
                    }
                }

                // 下载完成
                onDownloadComplete(libraryId, assetPath, outputFile.length(), countWordsInFile(outputFile))
            } catch (e: Exception) {
                Log.e(TAG, "Error copying from assets", e)
                throw e
            }
        }
    }

    private suspend fun downloadFromNetwork(libraryId: String, libraryName: String, downloadUrl: String) {
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(downloadUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.setRequestProperty("Accept-Encoding", "identity")
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("HTTP error: ${connection.responseCode}")
                }

                val contentLength = connection.contentLength.toLong()
                val inputStream = connection.inputStream
                val libraryDir = File(filesDir, "vocabularies")
                if (!libraryDir.exists()) {
                    libraryDir.mkdirs()
                }
                val outputFile = File(libraryDir, getFileNameFromUrl(downloadUrl))

                var downloadedBytes = 0L
                val buffer = ByteArray(8192)
                var bytes: Int

                inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        while (input.read(buffer).also { bytes = it } != -1) {
                            if (isPaused) {
                                while (isPaused) {
                                    delay(100)
                                }
                            }

                            output.write(buffer, 0, bytes)
                            downloadedBytes += bytes

                            // 更新进度
                            if (contentLength > 0) {
                                val progress = (downloadedBytes * 100 / contentLength).toInt()
                                updateNotificationProgress(libraryName, progress, downloadedBytes, contentLength)
                            }
                        }
                    }
                }

                // 下载完成
                onDownloadComplete(libraryId, outputFile.name, outputFile.length(), countWordsInFile(outputFile))
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun getFileNameFromUrl(url: String): String {
        return url.substring(url.lastIndexOf('/') + 1)
    }

    private fun countWordsInFile(file: File): Int {
        return try {
            val content = file.readText()
            // 简单估算：假设每个单词平均占用250字节
            content.length / 250
        } catch (e: Exception) {
            0
        }
    }

    private fun onDownloadComplete(libraryId: String, fileName: String, fileSize: Long, wordCount: Int) {
        // 通知下载完成
        notifySuccess(fileName)

        // 广播下载完成事件
        val intent = Intent(DOWNLOAD_STATUS_SUCCESS).apply {
            putExtra(EXTRA_LIBRARY_ID, libraryId)
            putExtra(EXTRA_LIBRARY_NAME, fileName)
        }
        sendBroadcast(intent)

        stopForeground(true)
        stopSelf()
    }

    private fun cancelDownload() {
        currentJob?.cancel()
        isDownloading = false
        stopForeground(true)
        stopSelf()
    }

    private fun pauseDownload() {
        isPaused = true
    }

    private fun updateNotificationProgress(libraryName: String, progress: Int, downloaded: Long, total: Long) {
        val notification = createProgressNotification(libraryName, progress, downloaded, total)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "词库下载",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示词库下载进度"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createProgressNotification(
        libraryName: String,
        progress: Int,
        downloaded: Long,
        total: Long
    ): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("正在下载：$libraryName")
            .setContentText("$progress%")
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setAutoCancel(false)

        return builder.build()
    }

    private fun notifySuccess(fileName: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("下载完成")
            .setContentText("词库 $fileName 下载成功")
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun notifyError(libraryName: String, error: String?) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("下载失败")
            .setContentText("词库 $libraryName 下载失败：${error ?: "未知错误"}")
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        currentJob?.cancel()
        Log.d(TAG, "LibraryDownloadService onDestroy")
    }
}
