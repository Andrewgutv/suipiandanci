package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.fragmentwords.data.WordRepository
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
        // "认识" - 刷新到下一个单词
        Log.d(TAG, "Word marked as known, refreshing to next word")
        val serviceIntent = Intent(context, WordService::class.java).apply {
            action = WordService.ACTION_SHOW_WORD
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun handleUnknown(context: Context, word: Word) {
        // "不认识" - 加入生词本，然后刷新到下一个单词
        Log.d(TAG, "Word: ${word.word} marked as unknown")
        CoroutineScope(Dispatchers.IO).launch {
            val repository = WordRepository(context)
            repository.addToNotebook(word)
            Log.d(TAG, "Added to notebook: ${word.word}")
        }
        // 刷新到下一个单词
        Log.d(TAG, "Refreshing to next word")
        val serviceIntent = Intent(context, WordService::class.java).apply {
            action = WordService.ACTION_SHOW_WORD
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun extractWord(intent: Intent): Word? {
        val word = intent.getStringExtra(WordService.EXTRA_WORD) ?: return null
        val phonetic = intent.getStringExtra(WordService.EXTRA_PHONETIC) ?: ""
        val translation = intent.getStringExtra(WordService.EXTRA_TRANSLATION) ?: ""
        val example = intent.getStringExtra(WordService.EXTRA_EXAMPLE) ?: ""
        return Word(word, phonetic, translation, example)
    }
}
