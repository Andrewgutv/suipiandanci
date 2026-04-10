package com.fragmentwords.service

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.fragmentwords.R
import com.fragmentwords.data.WordRepository
import com.fragmentwords.manager.LibraryManager
import com.fragmentwords.manager.LearningManager
import com.fragmentwords.model.Word
import com.fragmentwords.receiver.WordActionReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WordService : Service() {

    companion object {
        private const val TAG = "WordService"
        const val CHANNEL_ID = "word_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_SHOW_WORD = "com.fragmentwords.SHOW_WORD"
        const val ACTION_KNOWN = "com.fragmentwords.ACTION_KNOWN"
        const val ACTION_UNKNOWN = "com.fragmentwords.ACTION_UNKNOWN"
        const val ACTION_STOP = "com.fragmentwords.STOP_SERVICE"

        const val EXTRA_WORD = "extra_word"
        const val EXTRA_PHONETIC = "extra_phonetic"
        const val EXTRA_TRANSLATION = "extra_translation"
        const val EXTRA_EXAMPLE = "extra_example"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        const val EXTRA_PART_OF_SPEECH = "extra_part_of_speech"
        const val EXTRA_LIBRARY = "extra_library"

        fun startService(context: Context) {
            val intent = Intent(context, WordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context !is Activity) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, WordService::class.java))
        }

        fun showNewWord(context: Context) {
            val intent = Intent(context, WordService::class.java).apply {
                action = ACTION_SHOW_WORD
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context !is Activity) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var repository: WordRepository
    private lateinit var learningManager: LearningManager
    private lateinit var libraryManager: LibraryManager
    private lateinit var notificationManager: NotificationManager
    private var isFirstWord = true
    private var initialLoadScheduled = false
    private var updateJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WordService onCreate")

        repository = WordRepository(applicationContext)
        learningManager = LearningManager(applicationContext)
        libraryManager = LibraryManager(applicationContext)
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        startForegroundCompat(createPlaceholderNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_SHOW_WORD -> scheduleUpdate(forceRefresh = true)
            null -> {
                if (!initialLoadScheduled) {
                    initialLoadScheduled = true
                    scheduleUpdate(forceRefresh = false)
                } else {
                    Log.d(TAG, "Ignoring duplicate initial start command")
                }
            }

            else -> Log.w(TAG, "Unhandled action: ${intent.action}")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        updateJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
        Log.d(TAG, "WordService onDestroy")
    }

    private fun scheduleUpdate(forceRefresh: Boolean) {
        if (updateJob?.isActive == true) {
            if (!forceRefresh) {
                Log.d(TAG, "Skipping update because another update is already running")
                return
            }
            updateJob?.cancel()
        }

        updateJob = serviceScope.launch {
            repository.initializeIfNeeded()
            updateWordNotification(forceRefresh)
        }
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private suspend fun updateWordNotification(forceRefresh: Boolean) {
        try {
            val enabledLibraryIds = libraryManager.getEnabledLibraryIds()
            val selectedLibraries = if (enabledLibraryIds.isEmpty()) {
                listOf("CET4")
            } else {
                enabledLibraryIds.map { it.uppercase() }
            }

            val word = learningManager.getNextWord(selectedLibraries)
            if (word == null) {
                Log.w(TAG, "No words available for notification")
                return
            }

            repository.saveCurrentWord(word)
            notificationManager.notify(NOTIFICATION_ID, createWordNotification(word, isFirstWord))
            Log.d(TAG, "Updated word notification: ${word.word}, library=${word.library}, forceRefresh=$forceRefresh")
            isFirstWord = false
        } catch (e: Exception) {
            Log.e(TAG, "Error updating word notification", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_description)
                setShowBadge(false)
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createWordNotification(word: Word, isFirst: Boolean): Notification {
        val knownIntent = Intent(this, WordActionReceiver::class.java).apply {
            action = ACTION_KNOWN
            putWordExtras(word)
        }
        val unknownIntent = Intent(this, WordActionReceiver::class.java).apply {
            action = ACTION_UNKNOWN
            putWordExtras(word)
        }

        val knownPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            knownIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val unknownPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            unknownIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            append(word.phonetic)
            append("\n")
            append(word.translation)
            if (word.example.isNotBlank()) {
                append("\n\n例句: ")
                append(word.example)
            }
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(word.word)
            .setContentText(word.translation)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
                    .setBigContentTitle(word.word)
            )
            .setStyle(MediaStyle().setShowActionsInCompactView(0, 1))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setOngoing(false)
            .setAutoCancel(false)
            .addAction(android.R.drawable.ic_menu_add, getString(R.string.action_known), knownPendingIntent)
            .addAction(android.R.drawable.ic_menu_delete, getString(R.string.action_unknown), unknownPendingIntent)

        if (isFirst) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            builder.setVibrate(longArrayOf(0, 100, 50, 100))
        }

        return builder.build()
    }

    private fun Intent.putWordExtras(word: Word) {
        putExtra(EXTRA_WORD, word.word)
        putExtra(EXTRA_PHONETIC, word.phonetic)
        putExtra(EXTRA_TRANSLATION, word.translation)
        putExtra(EXTRA_EXAMPLE, word.example)
        putExtra(EXTRA_DIFFICULTY, word.difficulty)
        putExtra(EXTRA_PART_OF_SPEECH, word.partOfSpeech)
        putExtra(EXTRA_LIBRARY, word.library)
    }

    private fun createPlaceholderNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
