package com.fragmentwords.network

import com.fragmentwords.network.model.*

/**
 * API仓库 - 封装API调用
 */
class ApiRepository private constructor() {

    private val apiService = RetrofitClient.getClient().create(ApiService::class.java)

    companion object {
        @Volatile
        private var instance: ApiRepository? = null

        fun getInstance(): ApiRepository {
            return instance ?: synchronized(this) {
                instance ?: ApiRepository().also { instance = it }
            }
        }
    }

    // ============================================
    // 词库相关
    // ============================================

    suspend fun getVocabList(): Result<List<VocabResponse>> {
        return try {
            val response = apiService.getVocabList()
            if (response.isSuccessful && response.body()?.isSuccess() == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "获取词库列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // 单词相关
    // ============================================

    suspend fun getRandomWord(vocabId: Long? = null): Result<WordResponse> {
        return try {
            val response = apiService.getRandomWord(vocabId)
            if (response.isSuccessful && response.body()?.isSuccess() == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "获取单词失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // 学习进度相关
    // ============================================

    suspend fun getNextWord(
        deviceId: String? = getDeviceId(),
        userId: Long? = getUserId(),
        vocabIds: List<Long>? = null
    ): Result<LearningResponse> {
        return try {
            val request = if (vocabIds != null) NextWordRequest(vocabIds) else null
            val response = apiService.getNextWord(deviceId, userId, request)
            if (response.isSuccessful && response.body()?.isSuccess() == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "获取单词失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitFeedback(
        wordId: Long,
        isKnown: Boolean,
        deviceId: String? = getDeviceId(),
        userId: Long? = getUserId()
    ): Result<LearningResponse> {
        return try {
            val request = LearningRequest(wordId, isKnown)
            val response = apiService.submitFeedback(deviceId, userId, request)
            if (response.isSuccessful && response.body()?.isSuccess() == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "提交反馈失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLearningStats(
        deviceId: String? = getDeviceId(),
        userId: Long? = getUserId()
    ): Result<ProgressStatsResponse> {
        return try {
            val response = apiService.getLearningStats(deviceId, userId)
            if (response.isSuccessful && response.body()?.isSuccess() == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "获取统计失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // 用户相关
    // ============================================

    suspend fun login(
        username: String,
        password: String,
        deviceId: String? = getDeviceId()
    ): Result<LoginResponse> {
        return try {
            val request = LoginRequest(username, password, deviceId)
            val response = apiService.login(request)
            if (response.isSuccessful && response.body()?.isSuccess() == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "登录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        username: String,
        password: String,
        phone: String? = null,
        deviceId: String? = getDeviceId()
    ): Result<UserInfoResponse> {
        return try {
            val request = RegisterRequest(username, password, phone, deviceId)
            val response = apiService.register(request)
            if (response.isSuccessful && response.body()?.isSuccess() == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "注册失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // 工具方法
    // ============================================

    private fun getDeviceId(): String {
        // TODO: 从SharedPreferences获取设备ID
        // 暂时返回默认值
        return "android_device_${System.currentTimeMillis()}"
    }

    private fun getUserId(): Long? {
        // TODO: 从SharedPreferences获取用户ID
        // 暂时返回null（未登录状态）
        return null
    }
}
