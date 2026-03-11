package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.fragmentwords.data.WordRepository
import com.fragmentwords.manager.LearningManager
import com.fragmentwords.model.Word
import com.fragmentwords.service.WordService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 单词卡片按钮点击接收器
 */
class WordActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WordActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received action: $action")

        when (action) {
            WordService.ACTION_KNOWN -> {
                Log.d(TAG, "Handling KNOWN action")
                handleKnown(context)
            }
            WordService.ACTION_UNKNOWN -> {
                Log.d(TAG, "Handling UNKNOWN action")
                val word = extractWord(intent)
                if (word != null) {
                    handleUnknown(context, word)
                } else {
                    Log.e(TAG, "Failed to extract word from intent")
                }
            }
            else -> {
                Log.e(TAG, "Unknown action: $action")
            }
        }
    }

    private fun handleKnown(context: Context) {
        // "认识" - 使用艾宾浩斯算法记录学习进度
        Log.d(TAG, "Word marked as known, recording learning progress")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val learningManager = LearningManager(context)
                val repository = WordRepository(context)
                val currentWord = repository.getCurrentWord()

                if (currentWord != null) {
                    val advice = learningManager.handleUserFeedback(currentWord, "known")
                    Log.d(TAG, "Learning advice: $advice")

                    // 取消当前通知 ✅ 关键修改：点击后通知消失
                    val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    notificationManager.cancel(WordService.NOTIFICATION_ID)

                    Log.d(TAG, "Notification cancelled, will show next word on unlock")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling known: ${e.message}")
            }
        }

        // ✅ 移除：不再立即显示下一个单词
        // 下次解锁手机时，ScreenUnlockReceiver 会自动显示新单词
    }

    private fun handleUnknown(context: Context, word: Word) {
        // "不认识" - 使用艾宾浩斯算法记录学习进度，加入生词本
        Log.d(TAG, "Word: ${word.word} marked as unknown")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val learningManager = LearningManager(context)
                val repository = WordRepository(context)

                // 使用艾宾浩斯算法记录学习进度
                val advice = learningManager.handleUserFeedback(word, "unknown")
                Log.d(TAG, "Learning advice: $advice")

                // 加入生词本
                repository.addToNotebook(word)
                Log.d(TAG, "Added to notebook: ${word.word}")

                // 取消当前通知 ✅ 关键修改：点击后通知消失
                val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(WordService.NOTIFICATION_ID)

                Log.d(TAG, "Notification cancelled, will show next word on unlock")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling unknown: ${e.message}")
            }
        }

        // ✅ 移除：不再立即显示下一个单词
        // 下次解锁手机时，ScreenUnlockReceiver 会自动显示新单词
    }

    private fun extractWord(intent: Intent): Word? {
        val word = intent.getStringExtra(WordService.EXTRA_WORD) ?: return null
        val phonetic = intent.getStringExtra(WordService.EXTRA_PHONETIC) ?: ""
        val translation = intent.getStringExtra(WordService.EXTRA_TRANSLATION) ?: ""
        val example = intent.getStringExtra(WordService.EXTRA_EXAMPLE) ?: ""
        return Word(word, phonetic, translation, example)
    }
}
