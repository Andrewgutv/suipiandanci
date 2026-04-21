package com.fragmentwords.network

import com.fragmentwords.BuildConfig

object ApiConfig {
    const val BASE_URL = BuildConfig.API_BASE_URL
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
