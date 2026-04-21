package com.fragmentwords.network

import com.fragmentwords.network.model.ApiResponse
import com.fragmentwords.network.model.LearningRequest
import com.fragmentwords.network.model.LearningResponse
import com.fragmentwords.network.model.LoginRequest
import com.fragmentwords.network.model.LoginResponse
import com.fragmentwords.network.model.NextWordRequest
import com.fragmentwords.network.model.NotebookPageResponse
import com.fragmentwords.network.model.ProgressStatsResponse
import com.fragmentwords.network.model.RegisterRequest
import com.fragmentwords.network.model.UserInfoResponse
import com.fragmentwords.network.model.VocabResponse
import com.fragmentwords.network.model.VocabSelectRequest
import com.fragmentwords.network.model.VocabSelectionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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
    @GET("api/v1/vocabs")
    suspend fun getVocabList(): Response<ApiResponse<List<VocabResponse>>>

    @GET("api/v1/vocabs/current")
    suspend fun getCurrentVocab(
        @Header("X-Device-Id") deviceId: String
    ): Response<ApiResponse<VocabSelectionResponse>>

    @PUT("api/v1/vocabs/current")
    suspend fun updateCurrentVocab(
        @Header("X-Device-Id") deviceId: String,
        @Body request: VocabSelectRequest
    ): Response<ApiResponse<Void>>

    // ============================================
    // 单词相关接口
    // ============================================

    /**
     * 随机获取单词
     * @param vocabId 词库ID（可选）
     */

    // ============================================
    // 学习进度相关接口（艾宾浩斯算法）
    // ============================================

    /**
     * 获取下一个需要学习的单词
     */
    @POST("api/v1/learning/next")
    suspend fun getNextWord(
        @Header("X-Device-Id") deviceId: String?,
        @Header("Authorization") authorization: String?,
        @Body request: NextWordRequest?
    ): Response<ApiResponse<LearningResponse>>

    /**
     * 提交学习反馈
     */
    @POST("api/v1/learning/feedback")
    suspend fun submitFeedback(
        @Header("X-Device-Id") deviceId: String?,
        @Header("Authorization") authorization: String?,
        @Body feedback: LearningRequest
    ): Response<ApiResponse<LearningResponse>>

    /**
     * 获取学习统计
     */
    @GET("api/v1/learning/stats")
    suspend fun getLearningStats(
        @Header("X-Device-Id") deviceId: String?,
        @Header("Authorization") authorization: String?
    ): Response<ApiResponse<ProgressStatsResponse>>

    /**
     * 获取单词学习详情
     */
    @GET("api/v1/learning/word/{wordId}")
    suspend fun getWordProgress(
        @Path("wordId") wordId: Long,
        @Header("X-Device-Id") deviceId: String?,
        @Header("Authorization") authorization: String?
    ): Response<ApiResponse<LearningResponse>>

    // ============================================
    // 生词本相关接口
    // ============================================

    /**
     * 添加生词
     */
    @POST("api/v1/notebook")
    suspend fun addUnknownWord(
        @Header("X-Device-Id") deviceId: String?,
        @Header("Authorization") authorization: String?,
        @Query("wordId") wordId: Long
    ): Response<ApiResponse<Void>>

    /**
     * 获取生词本列表
     */
    @GET("api/v1/notebook")
    suspend fun getUnknownWords(
        @Header("X-Device-Id") deviceId: String?,
        @Header("Authorization") authorization: String?,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): Response<ApiResponse<NotebookPageResponse>>

    @GET("api/v1/notebook/count")
    suspend fun getUnknownWordCount(
        @Header("X-Device-Id") deviceId: String?,
        @Header("Authorization") authorization: String?
    ): Response<ApiResponse<Int>>

    /**
     * 删除生词
     */
    @DELETE("api/v1/notebook/{wordId}")
    suspend fun removeUnknownWord(
        @Path("wordId") wordId: Long,
        @Header("X-Device-Id") deviceId: String?,
        @Header("Authorization") authorization: String?
    ): Response<ApiResponse<Void>>

    // ============================================
    // 用户相关接口
    // ============================================

    /**
     * 用户注册
     */
    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<UserInfoResponse>>

    /**
     * 用户登录
     */
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    /**
     * 获取用户信息
     */
    @GET("api/v1/auth/info/{userId}")
    suspend fun getUserInfo(
        @Path("userId") userId: Long
    ): Response<ApiResponse<UserInfoResponse>>
}
