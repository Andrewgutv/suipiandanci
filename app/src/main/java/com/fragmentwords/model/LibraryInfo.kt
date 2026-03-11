package com.fragmentwords.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 词库信息数据类
 */
@Parcelize
data class LibraryInfo(
    val id: String,              // 词库ID（如：cet4, cet6, ielts等）
    val name: String,            // 词库名称（如：CET4核心词汇）
    val nameEn: String,          // 英文名称
    val description: String,     // 描述
    val size: Long,              // 文件大小（字节）
    val wordCount: Int,          // 词汇数量
    val fileName: String,        // 文件名（如：cet4_words.json）
    val downloadUrl: String,     // 下载URL（可以是本地assets或远程服务器）
    val isBuiltIn: Boolean,      // 是否内置（内置的不需要下载）
    val isDownloaded: Boolean,   // 是否已下载
    val isEnabled: Boolean,      // 是否启用（用户是否在学习这个词库）
    val icon: Int,               // 图标资源ID
    val difficulty: Int,         // 难度等级（1-5）
    val category: String,        // 分类（exam/ielts/toefl/grad）
    val version: Int             // 版本号
) : Parcelable {

    companion object {
        // 内置词库（预装在APK中）
        val BUILT_IN_LIBRARIES = listOf(
            LibraryInfo(
                id = "cet4",
                name = "CET4核心词汇",
                nameEn = "CET4 Core Vocabulary",
                description = "大学英语四级考试核心词汇，覆盖最常用的2000词",
                size = 100 * 1024, // 100KB
                wordCount = 450,
                fileName = "cet4_words.json",
                downloadUrl = "asset://cet4_words.json",
                isBuiltIn = true,
                isDownloaded = true,
                isEnabled = true,
                icon = android.R.drawable.ic_menu_info_details,
                difficulty = 2,
                category = "exam",
                version = 1
            ),
            LibraryInfo(
                id = "cet6",
                name = "CET6核心词汇",
                nameEn = "CET6 Core Vocabulary",
                description = "大学英语六级考试核心词汇，2500个高频词",
                size = 0,
                wordCount = 0,
                fileName = "cet6_words.json",
                downloadUrl = "https://your-server.com/vocabularies/cet6_words.json",
                isBuiltIn = false,
                isDownloaded = false,
                isEnabled = false,
                icon = android.R.drawable.ic_menu_edit,
                difficulty = 4,
                category = "exam",
                version = 1
            ),
            LibraryInfo(
                id = "ielts",
                name = "IELTS词汇",
                nameEn = "IELTS Vocabulary",
                description = "雅思考试核心词汇，涵盖听说读写",
                size = 0,
                wordCount = 0,
                fileName = "ielts_words.json",
                downloadUrl = "https://your-server.com/vocabularies/ielts_words.json",
                isBuiltIn = false,
                isDownloaded = false,
                isEnabled = false,
                icon = android.R.drawable.ic_menu_call,
                difficulty = 4,
                category = "ielts",
                version = 1
            ),
            LibraryInfo(
                id = "toefl",
                name = "TOEFL词汇",
                nameEn = "TOEFL Vocabulary",
                description = "托福考试核心词汇",
                size = 0,
                wordCount = 0,
                fileName = "toefl_words.json",
                downloadUrl = "https://your-server.com/vocabularies/toefl_words.json",
                isBuiltIn = false,
                isDownloaded = false,
                isEnabled = false,
                icon = android.R.drawable.ic_menu_camera,
                difficulty = 4,
                category = "toefl",
                version = 1
            ),
            LibraryInfo(
                id = "grad",
                name = "考研词汇",
                nameEn = "Graduate Exam Vocabulary",
                description = "研究生入学考试英语词汇",
                size = 0,
                wordCount = 0,
                fileName = "graduate_words.json",
                downloadUrl = "https://your-server.com/vocabularies/graduate_words.json",
                isBuiltIn = false,
                isDownloaded = false,
                isEnabled = false,
                icon = android.R.drawable.ic_menu_gallery,
                difficulty = 5,
                category = "exam",
                version = 1
            ),
            LibraryInfo(
                id = "gre",
                name = "GRE词汇",
                nameEn = "GRE Vocabulary",
                description = "GRE考试核心词汇，高级学术词汇",
                size = 0,
                wordCount = 0,
                fileName = "gre_words.json",
                downloadUrl = "https://your-server.com/vocabularies/gre_words.json",
                isBuiltIn = false,
                isDownloaded = false,
                isEnabled = false,
                icon = android.R.drawable.ic_menu_mapmode,
                difficulty = 5,
                category = "exam",
                version = 1
            )
        )
    }

    /**
     * 获取文件大小（格式化）
     */
    fun getFormattedSize(): String {
        val kb = size / 1024
        val mb = kb / 1024
        return when {
            mb > 0 -> "${mb}MB"
            kb > 0 -> "${kb}KB"
            else -> "${size}B"
        }
    }

    /**
     * 是否可下载
     */
    fun canDownload(): Boolean {
        return !isBuiltIn && !isDownloaded
    }

    /**
     * 是否可删除
     */
    fun canDelete(): Boolean {
        return !isBuiltIn && isDownloaded
    }
}
