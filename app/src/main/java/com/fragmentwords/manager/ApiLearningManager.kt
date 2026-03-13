package com.fragmentwords.manager

import android.content.Context
import android.util.Log
import com.fragmentwords.model.Word
import com.fragmentwords.network.ApiRepository
import com.fragmentwords.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * API学习管理器 - 使用后端API实现艾宾浩斯算法
 */
class ApiLearningManager(private val context: Context) {

    private val tag = "ApiLearningManager"
    private val apiRepository = ApiRepository.getInstance()
    private val prefsManager = PreferencesManager.getInstance(context)

    /**
     * 获取下一个需要学习的单词
     * 优先级：
     * 1. 需要复习的单词
     * 2. 新单词
     * @param vocabIds 选择的词库ID列表（可选）
     * @return 下一个单词
     */
    suspend fun getNextWord(vocabIds: List<Long>? = null): Word? = withContext(Dispatchers.IO) {
        try {
            val deviceId = prefsManager.getDeviceId()
            val userId = if (prefsManager.isLoggedIn()) prefsManager.getUserId() else null

            Log.d(tag, "Fetching next word: deviceId=$deviceId, userId=$userId, vocabIds=$vocabIds")

            val result = apiRepository.getNextWord(deviceId, userId, vocabIds)

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                val word = Word(
                    word = response.word,
                    phonetic = response.phonetic ?: "",
                    translation = response.translation,
                    example = response.example ?: "",
                    library = "CET4" // 暂时固定，可以从API返回
                )

                // 保存wordId映射，用于提交反馈
                saveWordIdMapping(response.word, response.wordId)

                Log.d(tag, "Got word: ${word.word}, stage: ${response.stage}")
                return@withContext word
            } else {
                val exception = result.exceptionOrNull()
                Log.e(tag, "Failed to get next word: ${exception?.message}")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error in getNextWord: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * 处理用户反馈
     * @param word 单词
     * @param isKnown 用户是否认识
     * @return 学习建议
     */
    suspend fun handleUserFeedback(word: Word, isKnown: Boolean): String = withContext(Dispatchers.IO) {
        try {
            val deviceId = prefsManager.getDeviceId()
            val userId = if (prefsManager.isLoggedIn()) prefsManager.getUserId() else null
            val wordId = getWordId(word.word)

            if (wordId == null) {
                Log.e(tag, "No wordId found for ${word.word}")
                return@withContext "提交失败"
            }

            Log.d(tag, "Submitting feedback: wordId=$wordId, isKnown=$isKnown")

            val result = apiRepository.submitFeedback(wordId, isKnown, deviceId, userId)

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                val advice = response.studyAdvice ?: "继续加油！"
                Log.d(tag, "Feedback submitted: stage=${response.stage}")
                return@withContext advice
            } else {
                val exception = result.exceptionOrNull()
                Log.e(tag, "Failed to submit feedback: ${exception?.message}")
                return@withContext "提交失败"
            }
        } catch (e: Exception) {
            Log.e(tag, "Error in handleUserFeedback: ${e.message}", e)
            return@withContext "提交失败"
        }
    }

    /**
     * 获取学习进度统计
     */
    suspend fun getLearningStats(): ProgressStats? = withContext(Dispatchers.IO) {
        try {
            val deviceId = prefsManager.getDeviceId()
            val userId = if (prefsManager.isLoggedIn()) prefsManager.getUserId() else null

            val result = apiRepository.getLearningStats(deviceId, userId)

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                return@withContext ProgressStats(
                    totalWords = response.totalWords,
                    masteredWords = response.masteredWords,
                    inReviewWords = response.inReviewWords,
                    needReviewWords = response.needReviewWords,
                    newWords = response.newWords,
                    avgRetentionRate = response.avgRetentionRate,
                    masteryRate = response.masteryRate
                )
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error in getLearningStats: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * 保存单词ID映射（用于提交反馈）
     */
    private fun saveWordIdMapping(word: String, wordId: Long) {
        val prefs = context.getSharedPreferences("word_id_mapping", Context.MODE_PRIVATE)
        prefs.edit().putLong(word, wordId).apply()
    }

    /**
     * 获取单词ID
     */
    private fun getWordId(word: String): Long? {
        val prefs = context.getSharedPreferences("word_id_mapping", Context.MODE_PRIVATE)
        return if (prefs.contains(word)) {
            prefs.getLong(word, -1)
        } else {
            null
        }
    }

    /**
     * 学习进度统计数据
     */
    data class ProgressStats(
        val totalWords: Int,
        val masteredWords: Int,
        val inReviewWords: Int,
        val needReviewWords: Int,
        val newWords: Int,
        val avgRetentionRate: Int,
        val masteryRate: Int
    )
}
