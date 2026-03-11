package com.fragmentwords.manager

import android.util.Log
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * 艾宾浩斯遗忘曲线算法管理器
 *
 * 核心原理：
 * 1. 8个复习节点：5分钟、30分钟、12小时、1天、2天、4天、7天、15天
 * 2. 用户点击"认识" → 进入下一个复习节点
 * 3. 用户点击"不认识" → 重置回第1个节点
 * 4. 完成所有8个节点 → 单词已掌握
 */
object EbbinghausManager {

    private const val TAG = "EbbinghausManager"

    /**
     * 8个复习节点（单位：毫秒）
     */
    private val REVIEW_INTERVALS = listOf(
        5 * 60 * 1000L,              // 5分钟
        30 * 60 * 1000L,             // 30分钟
        12 * 60 * 60 * 1000L,        // 12小时
        1 * 24 * 60 * 60 * 1000L,    // 1天
        2 * 24 * 60 * 60 * 1000L,    // 2天
        4 * 24 * 60 * 60 * 1000L,    // 4天
        7 * 24 * 60 * 60 * 1000L,    // 7天
        15 * 24 * 60 * 60 * 1000L    // 15天
    )

    /**
     * 复习节点描述
     */
    val REVIEW_STAGE_DESCRIPTIONS = listOf(
        "5分钟后复习",
        "30分钟后复习",
        "12小时后复习",
        "1天后复习",
        "2天后复习",
        "4天后复习",
        "7天后复习",
        "15天后复习"
    )

    /**
     * 计算下次复习时间
     * @param currentStage 当前复习阶段（0-7，0表示第一次学习）
     * @param isKnown 用户是否认识该单词
     * @return 下次复习时间戳（毫秒）
     */
    fun calculateNextReviewTime(currentStage: Int, isKnown: Boolean): Long {
        val now = System.currentTimeMillis()

        return if (isKnown) {
            // 认识：进入下一个复习阶段
            if (currentStage < REVIEW_INTERVALS.size) {
                now + REVIEW_INTERVALS[currentStage]
            } else {
                // 已完成所有复习阶段，设置为很久以后（表示已掌握）
                now + 365 * 24 * 60 * 60 * 1000L
            }
        } else {
            // 不认识：重置回第一个复习阶段（5分钟后）
            now + REVIEW_INTERVALS[0]
        }
    }

    /**
     * 计算下次复习阶段
     * @param currentStage 当前复习阶段
     * @param isKnown 用户是否认识该单词
     * @return 下次复习阶段（0-8，8表示已掌握）
     */
    fun calculateNextStage(currentStage: Int, isKnown: Boolean): Int {
        return if (isKnown) {
            minOf(currentStage + 1, 8) // 最大为8（已掌握）
        } else {
            0 // 重置回0
        }
    }

    /**
     * 检查单词是否需要复习
     * @param nextReviewTime 下次复习时间戳
     * @return 是否需要复习
     */
    fun needsReview(nextReviewTime: Long): Boolean {
        return System.currentTimeMillis() >= nextReviewTime
    }

    /**
     * 获取复习阶段的描述
     * @param stage 复习阶段（0-8）
     * @return 阶段描述
     */
    fun getStageDescription(stage: Int): String {
        return if (stage <= 7) {
            "第${stage + 1}次复习：${REVIEW_STAGE_DESCRIPTIONS.getOrElse(stage) { "未知" }}"
        } else {
            "已掌握"
        }
    }

    /**
     * 获取距离下次复习的时间描述
     * @param nextReviewTime 下次复习时间戳
     * @return 时间描述（如"2小时后"、"3天后"）
     */
    fun getTimeUntilReview(nextReviewTime: Long): String {
        val now = System.currentTimeMillis()
        val diff = nextReviewTime - now

        if (diff <= 0) return "现在"

        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            minutes < 60 -> "${minutes}分钟后"
            hours < 24 -> "${hours}小时后"
            days < 30 -> "${days}天后"
            else -> "${days / 30}个月后"
        }
    }

    /**
     * 计算记忆保持率（基于艾宾浩斯遗忘曲线）
     * @param stage 当前复习阶段
     * @return 记忆保持率（0-100）
     */
    fun calculateRetentionRate(stage: Int): Int {
        // 艾宾浩斯遗忘曲线：每次复习后记忆保持率提升
        val baseRates = listOf(20, 58, 72, 80, 85, 88, 90, 92, 95)
        return baseRates.getOrElse(stage) { 95 }
    }

    /**
     * 获取学习建议
     * @param stage 当前复习阶段
     * @param isKnown 用户是否认识
     * @return 学习建议
     */
    fun getStudyAdvice(stage: Int, isKnown: Boolean): String {
        return if (isKnown) {
            when {
                stage == 0 -> "很好！5分钟后复习一次"
                stage == 1 -> "不错！12小时后复习"
                stage == 2 -> "坚持！明天复习"
                stage <= 4 -> "继续加油！保持复习节奏"
                stage <= 7 -> "即将掌握！坚持最后几次复习"
                else -> "恭喜！这个单词已经掌握"
            }
        } else {
            "没关系，5分钟后会再次出现，加强记忆"
        }
    }

    /**
     * 创建学习记录
     * @param word 单词
     * @param isKnown 是否认识
     * @return 学习记录
     */
    fun createLearningRecord(word: String, currentStage: Int, isKnown: Boolean): LearningRecord {
        val nextStage = calculateNextStage(currentStage, isKnown)
        val nextReviewTime = calculateNextReviewTime(currentStage, isKnown)

        return LearningRecord(
            word = word,
            stage = nextStage,
            nextReviewTime = nextReviewTime,
            lastReviewTime = System.currentTimeMillis(),
            isKnown = isKnown,
            retentionRate = calculateRetentionRate(nextStage)
        )
    }

    /**
     * 学习记录数据类
     */
    data class LearningRecord(
        val word: String,
        val stage: Int,                      // 当前复习阶段（0-8）
        val nextReviewTime: Long,            // 下次复习时间戳
        val lastReviewTime: Long,            // 上次复习时间戳
        val isKnown: Boolean,                // 最后一次是否认识
        val retentionRate: Int               // 当前记忆保持率
    )

    /**
     * 批量获取需要复习的单词
     * @param allWords 所有单词及其学习记录
     * @param limit 限制返回数量
     * @return 需要复习的单词列表
     */
    fun getWordsToReview(
        allWords: List<Pair<String, LearningRecord>>,
        limit: Int = 20
    ): List<String> {
        return allWords
            .filter { (_, record) ->
                needsReview(record.nextReviewTime) && record.stage < 8
            }
            .sortedBy { (_, record) -> record.nextReviewTime }
            .take(limit)
            .map { (word, _) -> word }
    }

    /**
     * 获取学习进度统计
     * @param allWords 所有单词及其学习记录
     * @return 学习统计数据
     */
    fun getLearningProgress(allWords: List<LearningRecord>): ProgressStats {
        val total = allWords.size
        val mastered = allWords.count { it.stage >= 8 }
        val inReview = allWords.count { it.stage in 1..7 }
        val needReview = allWords.count { it.stage in 1..7 && needsReview(it.nextReviewTime) }
        val new = allWords.count { it.stage == 0 }

        val avgRetentionRate = if (total > 0) {
            allWords.map { it.retentionRate }.average().toInt()
        } else {
            0
        }

        return ProgressStats(
            totalWords = total,
            masteredWords = mastered,
            inReviewWords = inReview,
            needReviewWords = needReview,
            newWords = new,
            avgRetentionRate = avgRetentionRate,
            masteryRate = if (total > 0) (mastered * 100 / total) else 0
        )
    }

    /**
     * 学习进度统计数据
     */
    data class ProgressStats(
        val totalWords: Int,         // 总单词数
        val masteredWords: Int,      // 已掌握单词数
        val inReviewWords: Int,      // 复习中单词数
        val needReviewWords: Int,    // 待复习单词数
        val newWords: Int,           // 新单词数
        val avgRetentionRate: Int,   // 平均记忆保持率
        val masteryRate: Int         // 掌握率（百分比）
    )
}
