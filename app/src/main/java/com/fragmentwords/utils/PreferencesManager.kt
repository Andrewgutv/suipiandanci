package com.fragmentwords.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences管理器 - 用于保存用户配置和登录信息
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "fragment_words_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_TOKEN = "token"
        private const val KEY_PHONE = "phone"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // ============================================
    // 设备ID
    // ============================================

    fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = "android_${System.currentTimeMillis()}"
            saveDeviceId(deviceId)
        }
        return deviceId
    }

    fun saveDeviceId(deviceId: String) {
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }

    // ============================================
    // 用户登录信息
    // ============================================

    fun saveLoginInfo(userId: Long, username: String, token: String, phone: String? = null) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_TOKEN, token)
            putString(KEY_PHONE, phone)
            putBoolean(KEY_IS_LOGGED_IN, true)
        }.apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getPhone(): String? {
        return prefs.getString(KEY_PHONE, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearLoginInfo() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_TOKEN)
            remove(KEY_PHONE)
            putBoolean(KEY_IS_LOGGED_IN, false)
        }.apply()
    }

    // ============================================
    // 清除所有数据
    // ============================================

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
