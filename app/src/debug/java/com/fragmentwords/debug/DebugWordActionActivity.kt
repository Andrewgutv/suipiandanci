package com.fragmentwords.debug

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.fragmentwords.data.WordRepository
import com.fragmentwords.manager.LearningManager
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugWordActionActivity : Activity() {

    companion object {
        private const val TAG = "DebugWordActionActivity"
        const val EXTRA_DEBUG_ACTION = "debug_action"
        const val DEBUG_ACTION_UNKNOWN = "unknown"
        const val DEBUG_ACTION_KNOWN = "known"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentWord = WordRepository(this).getCurrentWord()
        if (currentWord == null) {
            Log.e(TAG, "Cannot trigger debug action because current_word is missing")
            finish()
            return
        }

        val debugAction = intent.getStringExtra(EXTRA_DEBUG_ACTION) ?: DEBUG_ACTION_UNKNOWN
        Log.d(TAG, "Triggering debug action=$debugAction for word=${currentWord.word}, remoteId=${currentWord.remoteId}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (debugAction) {
                    DEBUG_ACTION_KNOWN -> handleKnown(currentWord)
                    else -> handleUnknown(currentWord)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Debug action failed", e)
            } finally {
                finish()
            }
        }
    }

    private suspend fun handleKnown(word: com.fragmentwords.model.Word) {
        markButtonClick()
        val learningManager = LearningManager(this)
        val repository = WordRepository(this)
        val advice = learningManager.handleUserFeedback(word, "known")
        Log.d(TAG, "Known advice: $advice")
        val synced = repository.syncWordFeedback(word, isKnown = true)
        Log.d(TAG, "Known sync result for ${word.word}: $synced")
        repository.clearCurrentWord()
        if (AppPreferences.isNotificationEnabled(this)) {
            WordService.showNewWord(this, word.word)
        }
    }

    private suspend fun handleUnknown(word: com.fragmentwords.model.Word) {
        markButtonClick()
        val learningManager = LearningManager(this)
        val repository = WordRepository(this)
        val advice = learningManager.handleUserFeedback(word, "unknown")
        Log.d(TAG, "Unknown advice: $advice")
        val inserted = repository.addToNotebook(word)
        Log.d(TAG, "Local notebook insert result for ${word.word}: $inserted")
        val synced = repository.syncWordFeedback(word, isKnown = false)
        Log.d(TAG, "Unknown sync result for ${word.word}: $synced")
        repository.clearCurrentWord()
        if (AppPreferences.isNotificationEnabled(this)) {
            WordService.showNewWord(this, word.word)
        }
    }

    private fun markButtonClick() {
        getSharedPreferences("word_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("just_clicked_button", true)
            .putLong("button_click_time", System.currentTimeMillis())
            .apply()
    }
}
