package com.fragmentwords.network.model

import com.google.gson.annotations.SerializedName

/**
 * 统一API响应格式
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T?
) {
    /**
     * 判断是否成功
     */
    fun isSuccess(): Boolean = code == 200
}

/**
 * 词库响应
 */
data class VocabResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("wordCount")
    val wordCount: Int,

    @SerializedName("description")
    val description: String?
)

/**
 * 单词响应
 */
data class WordResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("word")
    val word: String,

    @SerializedName("phonetic")
    val phonetic: String?,

    @SerializedName("translation")
    val translation: String,

    @SerializedName("example")
    val example: String?,

    @SerializedName("vocabId")
    val vocabId: Long
)

/**
 * 学习响应（艾宾浩斯算法）
 */
data class LearningResponse(
    @SerializedName("wordId")
    val wordId: Long,

    @SerializedName("word")
    val word: String,

    @SerializedName("phonetic")
    val phonetic: String?,

    @SerializedName("translation")
    val translation: String,

    @SerializedName("example")
    val example: String?,

    @SerializedName("stage")
    val stage: Int,

    @SerializedName("stageDescription")
    val stageDescription: String?,

    @SerializedName("nextReviewTime")
    val nextReviewTime: String?,

    @SerializedName("timeUntilReview")
    val timeUntilReview: String?,

    @SerializedName("retentionRate")
    val retentionRate: Int,

    @SerializedName("studyAdvice")
    val studyAdvice: String?,

    @SerializedName("isMastered")
    val isMastered: Boolean
)

/**
 * 学习进度统计响应
 */
data class ProgressStatsResponse(
    @SerializedName("totalWords")
    val totalWords: Int,

    @SerializedName("masteredWords")
    val masteredWords: Int,

    @SerializedName("inReviewWords")
    val inReviewWords: Int,

    @SerializedName("needReviewWords")
    val needReviewWords: Int,

    @SerializedName("newWords")
    val newWords: Int,

    @SerializedName("avgRetentionRate")
    val avgRetentionRate: Int,

    @SerializedName("masteryRate")
    val masteryRate: Int
)

/**
 * 用户登录响应
 */
data class LoginResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("token")
    val token: String,

    @SerializedName("phone")
    val phone: String?
)

/**
 * 用户信息响应
 */
data class UserInfoResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("deviceId")
    val deviceId: String?
)

/**
 * 学习请求
 */
data class LearningRequest(
    @SerializedName("wordId")
    val wordId: Long,

    @SerializedName("isKnown")
    val isKnown: Boolean
)

/**
 * 获取下一个单词请求
 */
data class NextWordRequest(
    @SerializedName("vocabIds")
    val vocabIds: List<Long>? = null
)

/**
 * 用户登录请求
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("deviceId")
    val deviceId: String? = null
)

/**
 * 用户注册请求
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("deviceId")
    val deviceId: String? = null
)
