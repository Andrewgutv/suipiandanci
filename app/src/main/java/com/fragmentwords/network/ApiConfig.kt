package com.fragmentwords.network

/**
 * API配置
 */
object MainApiConfigPlaceholder {
    // 基础URL（本地开发环境）
    const val BASE_URL = "http://10.0.2.2:8080/" // 模拟器访问本地主机

    // 如果使用真机测试，改为电脑的局域网IP
    // const val BASE_URL = "http://192.168.1.100:8080/"

    // 连接超时时间（秒）
    const val CONNECT_TIMEOUT = 30L

    // 读取超时时间（秒）
    const val READ_TIMEOUT = 30L

    // 写入超时时间（秒）
    const val WRITE_TIMEOUT = 30L
}
