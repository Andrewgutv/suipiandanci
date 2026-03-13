package com.fragmentwords.network

import com.fragmentwords.network.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API服务接口
 */
interface ApiService {

    // ============================================
    // 词库相关接口
    // ============================================

    /**
     * 获取所有词库
     */
    @GET("api/vocab/list")
    suspend fun getVocabList(): Response<ApiResponse<List<VocabResponse>>>

    // ============================================
    // 单词相关接口
    // ============================================

    /**
     * 随机获取单词
     * @param vocabId 词库ID（可选）
     */
    @GET("api/word/random")
    suspend fun getRandomWord(
        @Query("vocabId") vocabId: Long? = null
    ): Response<ApiResponse<WordResponse>>

    // ============================================
    // 学习进度相关接口（艾宾浩斯算法）
    // ============================================

    /**
     * 获取下一个需要学习的单词
     */
    @POST("api/learning/next")
    suspend fun getNextWord(
        @Header("X-Device-ID") deviceId: String?,
        @Header("X-User-ID") userId: Long?,
        @Body request: NextWordRequest?
    ): Response<ApiResponse<LearningResponse>>

    /**
     * 提交学习反馈
     */
    @POST("api/learning/feedback")
    suspend fun submitFeedback(
        @Header("X-Device-ID") deviceId: String?,
        @Header("X-User-ID") userId: Long?,
        @Body feedback: LearningRequest
    ): Response<ApiResponse<LearningResponse>>

    /**
     * 获取学习统计
     */
    @GET("api/learning/stats")
    suspend fun getLearningStats(
        @Header("X-Device-ID") deviceId: String?,
        @Header("X-User-ID") userId: Long?
    ): Response<ApiResponse<ProgressStatsResponse>>

    /**
     * 获取单词学习详情
     */
    @GET("api/learning/word/{wordId}")
    suspend fun getWordProgress(
        @Path("wordId") wordId: Long,
        @Header("X-Device-ID") deviceId: String?,
        @Header("X-User-ID") userId: Long?
    ): Response<ApiResponse<LearningResponse>>

    // ============================================
    // 生词本相关接口
    // ============================================

    /**
     * 添加生词
     */
    @POST("api/unknown/add")
    suspend fun addUnknownWord(
        @Header("X-Device-ID") deviceId: String?,
        @Header("X-User-ID") userId: Long?,
        @Query("wordId") wordId: Long
    ): Response<ApiResponse<Void>>

    /**
     * 获取生词本列表
     */
    @GET("api/unknown/list")
    suspend fun getUnknownWords(
        @Header("X-Device-ID") deviceId: String?,
        @Header("X-User-ID") userId: Long?
    ): Response<ApiResponse<List<WordResponse>>>

    /**
     * 删除生词
     */
    @DELETE("api/unknown/remove/{wordId}")
    suspend fun removeUnknownWord(
        @Path("wordId") wordId: Long,
        @Header("X-Device-ID") deviceId: String?,
        @Header("X-User-ID") userId: Long?
    ): Response<ApiResponse<Void>>

    // ============================================
    // 用户相关接口
    // ============================================

    /**
     * 用户注册
     */
    @POST("api/user/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<UserInfoResponse>>

    /**
     * 用户登录
     */
    @POST("api/user/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    /**
     * 获取用户信息
     */
    @GET("api/user/info/{userId}")
    suspend fun getUserInfo(
        @Path("userId") userId: Long
    ): Response<ApiResponse<UserInfoResponse>>
}
