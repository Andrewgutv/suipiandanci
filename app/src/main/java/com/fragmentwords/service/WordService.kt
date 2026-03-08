package com.fragmentwords.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.fragmentwords.R
import com.fragmentwords.data.WordRepository
import com.fragmentwords.model.Word
import com.fragmentwords.receiver.WordActionReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 单词服务 - 前台服务，单词卡片常驻通知栏
 */
class WordService : Service() {

    companion object {
        private const val TAG = "WordService"
        const val CHANNEL_ID = "word_channel"
        const val NOTIFICATION_ID = 1001

        // Android 14+ 前台服务类型常量
        private const val FOREGROUND_SERVICE_TYPE_DATA_SYNC = 1

        const val ACTION_SHOW_WORD = "com.fragmentwords.SHOW_WORD"
        const val ACTION_KNOWN = "com.fragmentwords.ACTION_KNOWN"
        const val ACTION_UNKNOWN = "com.fragmentwords.ACTION_UNKNOWN"
        const val ACTION_STOP = "com.fragmentwords.STOP_SERVICE"

        const val EXTRA_WORD = "extra_word"
        const val EXTRA_PHONETIC = "extra_phonetic"
        const val EXTRA_TRANSLATION = "extra_translation"
        const val EXTRA_EXAMPLE = "extra_example"

        fun startService(context: Context) {
            val intent = Intent(context, WordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WordService::class.java)
            context.stopService(intent)
        }

        fun showNewWord(context: Context) {
            val intent = Intent(context, WordService::class.java).apply {
                action = ACTION_SHOW_WORD
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun dismissNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
            Log.d(TAG, "Notification dismissed")
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var repository: WordRepository
    private lateinit var notificationManager: NotificationManager
    private var currentWord: Word? = null
    private var isFirstWord = true // 标记是否是第一个单词

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WordService onCreate")
        try {
            // 重置状态标志，确保服务重启后正常工作
            isFirstWord = true
            repository = WordRepository(applicationContext)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE)
            if (nm is NotificationManager) {
                notificationManager = nm
            } else {
                Log.e(TAG, "Failed to get NotificationManager")
                stopSelf()
                return
            }

            createNotificationChannel()

            // 立即启动前台服务，使用占位通知（避免5秒超时）
            val placeholderNotification = createPlaceholderNotification()

            // Android 14+ (API 34) 需要指定前台服务类型
            if (Build.VERSION.SDK_INT >= 34) {
                try {
                    // 使用反射调用新的API
                    val method = Service::class.java.getMethod(
                        "startForeground",
                        Int::class.java,
                        Notification::class.java,
                        Int::class.java
                    )
                    method.invoke(this, NOTIFICATION_ID, placeholderNotification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to call startForeground with type", e)
                    // 回退到旧API
                    startForeground(NOTIFICATION_ID, placeholderNotification)
                }
            } else {
                startForeground(NOTIFICATION_ID, placeholderNotification)
            }

            Log.d(TAG, "Foreground service started with placeholder")

            // 然后异步加载单词
            serviceScope.launch {
                try {
                    repository.initializeIfNeeded()
                    val word = repository.getNextWordSync()
                    if (word != null) {
                        currentWord = word
                        val notification = createWordNotification(word, true)
                        notificationManager.notify(NOTIFICATION_ID, notification)
                        Log.d(TAG, "Updated foreground notification with word: ${word.word}")
                    } else {
                        Log.w(TAG, "No words available, keeping placeholder")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading word: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WordService onStartCommand, action: ${intent?.action}")

        when (intent?.action) {
            ACTION_SHOW_WORD -> {
                Log.d(TAG, "Received ACTION_SHOW_WORD, updating word notification")
                serviceScope.launch {
                    repository.initializeIfNeeded()
                    updateWordNotification()
                }
            }
            ACTION_STOP -> {
                Log.d(TAG, "Received ACTION_STOP, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                Log.d(TAG, "No action, updating word notification")
                serviceScope.launch {
                    repository.initializeIfNeeded()
                    updateWordNotification()
                }
            }
        }

        return START_STICKY // 保持服务运行
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WordService onDestroy")
    }

    /**
     * 更新单词通知（不停止服务）
     */
    private fun updateWordNotification() {
        // 获取新单词时排除当前单词，避免重复
        val word = repository.getNextWordSync(excludeWord = currentWord?.word)
        if (word != null) {
            currentWord = word
            val notification = createWordNotification(word, isFirstWord)
            notificationManager.notify(NOTIFICATION_ID, notification)
            isFirstWord = false // 第一个单词已显示
            Log.d(TAG, "Updated word: ${word.word} from ${word.library}")
        } else {
            Log.w(TAG, "No words available")
        }
        // 不停止服务，保持前台通知
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_MAX  // 最高重要性，横幅通知
            ).apply {
                description = getString(R.string.channel_description)
                setShowBadge(false)
                enableVibration(true)  // 启用震动
                enableLights(true)      // 启用呼吸灯
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建单词卡片通知（使用媒体通知样式，类似音乐播放器）
     */
    private fun createWordNotification(word: Word, isFirst: Boolean = false): Notification {
        // "认识" 按钮
        val knownIntent = Intent(this, WordActionReceiver::class.java).apply {
            action = ACTION_KNOWN
            putExtra(EXTRA_WORD, word.word)
        }
        val knownPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            knownIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "不认识" 按钮
        val unknownIntent = Intent(this, WordActionReceiver::class.java).apply {
            action = ACTION_UNKNOWN
            putExtra(EXTRA_WORD, word.word)
            putExtra(EXTRA_PHONETIC, word.phonetic)
            putExtra(EXTRA_TRANSLATION, word.translation)
            putExtra(EXTRA_EXAMPLE, word.example)
        }
        val unknownPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            unknownIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 创建媒体通知样式 - 关键！让系统识别为媒体内容
        val mediaStyle = MediaStyle()
            .setShowActionsInCompactView(0, 1) // 在折叠视图显示2个按钮

        // 创建内容文本
        val contentText = StringBuilder()
        contentText.append(word.phonetic)
        contentText.append("\n").append(word.translation)
        if (word.example.isNotEmpty()) {
            contentText.append("\n\n例句：").append(word.example)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(word.word)
            .setContentText(word.phonetic)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText.toString())
                    .setBigContentTitle(word.word)
            )
            .setStyle(mediaStyle) // 使用媒体样式
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 锁屏可见
            .setPriority(NotificationCompat.PRIORITY_MAX) // 最高优先级
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT) // 媒体传输类别
            .setOngoing(true) // 不可滑动删除，常驻通知栏
            .setAutoCancel(false) // 点击不自动取消

        // 只在第一个单词时有声音和震动
        if (isFirst) {
            builder.setVibrate(longArrayOf(0, 100, 50, 100))
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        return builder
            .addAction(
                android.R.drawable.ic_menu_add,
                getString(R.string.action_known),
                knownPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_delete,
                getString(R.string.action_unknown),
                unknownPendingIntent
            )
            .build()
    }

    /**
     * 创建占位通知（当没有单词时）
     */
    private fun createPlaceholderNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("碎片单词")
            .setContentText("正在加载单词...")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("正在加载单词库，请稍候"))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(true)
            .setAutoCancel(false)

        return builder.build()
    }
}
