package com.fragmentwords.receiver

import android.content.BroadcastReceiver
import android.content.BroadcastReceiver.PendingResult
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.fragmentwords.data.WordRepository
import com.fragmentwords.manager.LearningManager
import com.fragmentwords.model.Word
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WordActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WordActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received action: $action")
        val pendingResult = goAsync()

        when (action) {
            WordService.ACTION_KNOWN -> {
                val word = extractWord(context, intent)
                if (word != null) {
                    handleKnown(context, word, pendingResult)
                } else {
                    Log.e(TAG, "Failed to extract word for known action")
                    pendingResult.finish()
                }
            }

            WordService.ACTION_UNKNOWN -> {
                val word = extractWord(context, intent)
                if (word != null) {
                    handleUnknown(context, word, pendingResult)
                } else {
                    Log.e(TAG, "Failed to extract word for unknown action")
                    pendingResult.finish()
                }
            }

            else -> {
                Log.e(TAG, "Unknown action: $action")
                pendingResult.finish()
            }
        }
    }

    private fun handleKnown(
        context: Context,
        word: Word,
        pendingResult: PendingResult
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Handling known for word=${word.word}, remoteId=${word.remoteId}")
                markButtonClick(context)

                val learningManager = LearningManager(context)
                val repository = WordRepository(context)
                val advice = learningManager.handleUserFeedback(word, "known")
                Log.d(TAG, "Learning advice: $advice")

                val synced = repository.syncWordFeedback(word, isKnown = true)
                Log.d(TAG, "Known feedback sync result for ${word.word}: $synced")
                repository.clearCurrentWord()
                if (AppPreferences.isNotificationEnabled(context)) {
                    WordService.showNewWord(context, word.word)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling known action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleUnknown(
        context: Context,
        word: Word,
        pendingResult: PendingResult
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Handling unknown for word=${word.word}, remoteId=${word.remoteId}")
                markButtonClick(context)

                val learningManager = LearningManager(context)
                val repository = WordRepository(context)
                val advice = learningManager.handleUserFeedback(word, "unknown")
                Log.d(TAG, "Learning advice: $advice")

                val inserted = repository.addToNotebook(word)
                val existsInNotebook = inserted || repository.getNotebookWords().any { it.word == word.word }

                val synced = repository.syncWordFeedback(word, isKnown = false)
                Log.d(TAG, "Unknown feedback sync result for ${word.word}: $synced")
                repository.clearCurrentWord()
                if (AppPreferences.isNotificationEnabled(context)) {
                    WordService.showNewWord(context, word.word)
                }
                showUnknownResultToast(context, inserted, existsInNotebook)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling unknown action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun markButtonClick(context: Context) {
        context.getSharedPreferences("word_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("just_clicked_button", true)
            .putLong("button_click_time", System.currentTimeMillis())
            .apply()
    }

    private suspend fun showUnknownResultToast(
        context: Context,
        inserted: Boolean,
        existsInNotebook: Boolean
    ) {
        withContext(Dispatchers.Main) {
            val message = when {
                inserted -> "已添加到生词本"
                existsInNotebook -> "该单词已在生词本中"
                else -> "添加到生词本失败"
            }
            Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractWord(context: Context, intent: Intent): Word? {
        val remoteId = intent.getLongExtra(WordService.EXTRA_WORD_ID, -1L)
        val word = intent.getStringExtra(WordService.EXTRA_WORD) ?: return null
        val phonetic = intent.getStringExtra(WordService.EXTRA_PHONETIC) ?: ""
        val translation = intent.getStringExtra(WordService.EXTRA_TRANSLATION) ?: ""
        val example = intent.getStringExtra(WordService.EXTRA_EXAMPLE) ?: ""
        val difficulty = intent.getIntExtra(WordService.EXTRA_DIFFICULTY, 1)
        val partOfSpeech = intent.getStringExtra(WordService.EXTRA_PART_OF_SPEECH) ?: ""
        val library = intent.getStringExtra(WordService.EXTRA_LIBRARY) ?: ""
        val fallbackCurrentWord = WordRepository(context).getCurrentWord()
        if (fallbackCurrentWord != null && fallbackCurrentWord.word != word) {
            Log.w(
                TAG,
                "Notification action word mismatch, intentWord=$word, currentWord=${fallbackCurrentWord.word}; using currentWord"
            )
            return fallbackCurrentWord
        }
        val resolvedRemoteId = remoteId.takeIf { it > 0 }
            ?: fallbackCurrentWord?.takeIf { it.word == word }?.remoteId
        return Word(
            remoteId = resolvedRemoteId,
            word = word,
            phonetic = phonetic,
            translation = translation,
            example = example,
            difficulty = difficulty,
            partOfSpeech = partOfSpeech,
            library = library.ifBlank { fallbackCurrentWord?.library.orEmpty() }
        ).also {
            if (it.remoteId == null) {
                Log.w(TAG, "Notification action received without remoteId for word=${it.word}")
            }
        }
    }
}
