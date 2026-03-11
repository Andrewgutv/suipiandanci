package com.fragmentwords.manager

import android.content.Context
import android.util.Log
import com.fragmentwords.database.WordDatabase
import com.fragmentwords.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 学习管理器 - 整合艾宾浩斯算法
 *
 * 功能：
 * 1. 记录用户学习行为（认识/不认识）
 * 2. 根据艾宾浩斯算法计算复习时间
 * 3. 优先推荐需要复习的单词
 */
class LearningManager(context: Context) {

    private val database = WordDatabase(context)
    private val tag = "LearningManager"

    /**
     * 处理用户反馈
     * @param word 单词
     * @param isKnown 用户是否认识
     * @return 学习建议
     */
    suspend fun handleUserFeedback(word: Word, isKnown: String): String = withContext(Dispatchers.IO) {
        val isKnownBool = isKnown == "known"
        val progress = database.getLearningProgress(word.word)

        val currentStage = progress?.stage ?: 0
        val nextStage = EbbinghausManager.calculateNextStage(currentStage, isKnownBool)
        val nextReviewTime = EbbinghausManager.calculateNextReviewTime(currentStage, isKnownBool)

        // 更新学习进度
        val newProgress = WordDatabase.LearningProgress(
            word = word.word,
            stage = nextStage,
            nextReviewTime = nextReviewTime,
            lastReviewTime = System.currentTimeMillis(),
            reviewCount = (progress?.reviewCount ?: 0) + 1,
            knownCount = (progress?.knownCount ?: 0) + if (isKnownBool) 1 else 0,
            unknownCount = (progress?.unknownCount ?: 0) + if (!isKnownBool) 1 else 0,
            createdTime = progress?.createdTime ?: System.currentTimeMillis()
        )

        database.updateLearningProgress(newProgress)

        val advice = EbbinghausManager.getStudyAdvice(nextStage, isKnownBool)
        Log.d(tag, "${word.word}: stage=$currentStage -> $nextStage, isKnown=$isKnownBool")

        return@withContext advice
    }

    /**
     * 获取下一个需要学习的单词
     * 优先级：
     * 1. 需要复习的单词
     * 2. 新单词
     * @param libraries 选择的词库
     * @return 下一个单词
     */
    suspend fun getNextWord(libraries: List<String>): Word? = withContext(Dispatchers.IO) {
        // 1. 获取需要复习的单词
        val reviewWords = if (libraries.isEmpty()) {
            database.getWordsToReview(limit = 10)
        } else {
            database.getWordsToReviewByLibraries(libraries, limit = 10)
        }

        if (reviewWords.isNotEmpty()) {
            // 从复习单词中随机选择一个
            val randomWord = reviewWords.random()
            Log.d(tag, "Reviewing word: $randomWord")
            return@withContext database.getWordByName(randomWord)
        }

        // 2. 没有需要复习的单词，选择新单词
        val newWord = if (libraries.isEmpty()) {
            database.getRandomWord()
        } else {
            database.getRandomWordByLibraries(libraries)
        }

        // 初始化新单词的学习进度
        if (newWord != null) {
            val progress = database.getLearningProgress(newWord.word)
            if (progress == null) {
                val initialProgress = WordDatabase.LearningProgress(
                    word = newWord.word,
                    stage = 0,
                    nextReviewTime = System.currentTimeMillis(), // 立即学习
                    lastReviewTime = 0,
                    reviewCount = 0,
                    knownCount = 0,
                    unknownCount = 0,
                    createdTime = System.currentTimeMillis()
                )
                database.updateLearningProgress(initialProgress)
                Log.d(tag, "New word: ${newWord.word}")
            }
        }

        return@withContext newWord
    }

    /**
     * 获取学习进度统计
     */
    suspend fun getLearningStats(): EbbinghausManager.ProgressStats = withContext(Dispatchers.IO) {
        val dbStats = database.getLearningStats()

        // 注意：这里需要从数据库查询所有记录以计算详细统计，简化起见直接返回基本统计

        return@withContext com.fragmentwords.manager.EbbinghausManager.ProgressStats(
            totalWords = dbStats.totalWords,
            masteredWords = dbStats.masteredWords,
            inReviewWords = dbStats.totalWords - dbStats.masteredWords - dbStats.newWords,
            needReviewWords = dbStats.needReviewWords,
            newWords = dbStats.newWords,
            avgRetentionRate = 0, // 可以后续实现
            masteryRate = dbStats.masteryRate
        )
    }

    /**
     * 获取单词的学习进度
     */
    suspend fun getWordProgress(word: String): WordDatabase.LearningProgress? = withContext(Dispatchers.IO) {
        return@withContext database.getLearningProgress(word)
    }

    /**
     * 清除所有学习进度
     */
    suspend fun clearAllProgress(): Int = withContext(Dispatchers.IO) {
        return@withContext database.clearAllProgress()
    }

    /**
     * 获取需要复习的单词数量
     */
    suspend fun getReviewCount(): Int = withContext(Dispatchers.IO) {
        return@withContext database.getLearningStats().needReviewWords
    }

    /**
     * 获取已掌握的单词数量
     */
    suspend fun getMasteredCount(): Int = withContext(Dispatchers.IO) {
        return@withContext database.getLearningStats().masteredWords
    }

    /**
     * 获取总学习单词数
     */
    suspend fun getTotalLearnedCount(): Int = withContext(Dispatchers.IO) {
        return@withContext database.getLearningStats().totalWords
    }
}
