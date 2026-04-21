package com.fragmentwords.manager

import android.content.Context
import android.util.Log
import com.fragmentwords.model.LibraryInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 词库管理器
 *
 * 功能：
 * 1. 管理词库下载状态
 * 2. 管理词库启用状态
 * 3. 提供词库列表
 * 4. 获取已启用的词库ID列表
 */
class LibraryManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "library_prefs"
        private const val KEY_LIBRARY_LIST = "library_list"
        private const val TAG = "LibraryManager"

        // 词库存储目录
        private const val LIBRARY_DIR = "vocabularies"
    }

    /**
     * 获取所有词库信息
     */
    suspend fun getAllLibraries(): List<LibraryInfo> = withContext(Dispatchers.IO) {
        val cached = getCachedLibraries()
        if (cached.isNotEmpty()) {
            val merged = mergeWithBuiltInLibraries(cached)
            if (merged != cached) {
                saveLibraries(merged)
            }
            return@withContext merged
        }

        // 使用内置词库列表
        val libraries = LibraryInfo.BUILT_IN_LIBRARIES
        saveLibraries(libraries)
        return@withContext libraries
    }

    /**
     * 获取已下载的词库
     */
    suspend fun getDownloadedLibraries(): List<LibraryInfo> = withContext(Dispatchers.IO) {
        getAllLibraries().filter { it.isDownloaded }
    }

    /**
     * 获取已启用的词库
     */
    suspend fun getEnabledLibraries(): List<LibraryInfo> = withContext(Dispatchers.IO) {
        getAllLibraries().filter { it.isEnabled && it.isDownloaded }
    }

    /**
     * 获取已启用的词库ID列表
     */
    suspend fun getEnabledLibraryIds(): List<String> = withContext(Dispatchers.IO) {
        getEnabledLibraries().map { it.id }
    }

    /**
     * 启用/禁用词库
     */
    suspend fun toggleLibrary(libraryId: String, enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val libraries = getAllLibraries().toMutableList()
            val index = libraries.indexOfFirst { it.id == libraryId }

            if (index == -1) {
                Log.w(TAG, "Library not found: $libraryId")
                return@withContext false
            }

            val library = libraries[index]
            val updatedLibrary = library.copy(isEnabled = enabled)
            libraries[index] = updatedLibrary

            saveLibraries(libraries)
            Log.d(TAG, "Library ${library.id} ${if (enabled) "enabled" else "disabled"}")

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling library", e)
            return@withContext false
        }
    }

    /**
     * 标记词库为已下载
     */
    suspend fun markAsDownloaded(
        libraryId: String,
        fileName: String,
        fileSize: Long,
        wordCount: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val libraries = getAllLibraries().toMutableList()
            val index = libraries.indexOfFirst { it.id == libraryId }

            if (index == -1) {
                Log.w(TAG, "Library not found: $libraryId")
                return@withContext false
            }

            val library = libraries[index]
            val updatedLibrary = library.copy(
                isDownloaded = true,
                fileName = fileName,
                size = fileSize,
                wordCount = wordCount
            )
            libraries[index] = updatedLibrary

            saveLibraries(libraries)
            Log.d(TAG, "Library ${library.id} marked as downloaded")

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking as downloaded", e)
            return@withContext false
        }
    }

    /**
     * 删除词库
     */
    suspend fun deleteLibrary(libraryId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val libraries = getAllLibraries().toMutableList()
            val index = libraries.indexOfFirst { it.id == libraryId }

            if (index == -1) {
                Log.w(TAG, "Library not found: $libraryId")
                return@withContext false
            }

            val library = libraries[index]

            // 删除文件
            val libraryFile = getLibraryFile(library.fileName)
            if (libraryFile.exists()) {
                libraryFile.delete()
                Log.d(TAG, "Deleted file: ${library.fileName}")
            }

            // 更新状态
            val updatedLibrary = library.copy(
                isDownloaded = false,
                isEnabled = false,
                size = 0,
                wordCount = 0
            )
            libraries[index] = updatedLibrary

            saveLibraries(libraries)
            Log.d(TAG, "Library ${library.id} deleted")

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting library", e)
            return@withContext false
        }
    }

    /**
     * 获取词库文件
     */
    fun getLibraryFile(fileName: String): File {
        val libraryDir = File(context.filesDir, LIBRARY_DIR)
        if (!libraryDir.exists()) {
            libraryDir.mkdirs()
        }
        return File(libraryDir, fileName)
    }

    /**
     * 从assets获取内置词库文件
     */
    suspend fun loadBuiltInLibrary(fileName: String): List<com.fragmentwords.model.Word>? = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("data/$fileName").bufferedReader().use { it.readText() }
            val wordType = object : TypeToken<List<com.fragmentwords.model.Word>>() {}.type
            val words = gson.fromJson<List<com.fragmentwords.model.Word>>(jsonString, wordType)
            Log.d(TAG, "Loaded ${words.size} words from asset: $fileName")
            return@withContext words
        } catch (e: Exception) {
            Log.e(TAG, "Error loading built-in library: $fileName", e)
            return@withContext null
        }
    }

    /**
     * 从本地文件加载词库
     */
    suspend fun loadLocalLibrary(fileName: String): List<com.fragmentwords.model.Word>? = withContext(Dispatchers.IO) {
        try {
            val libraryFile = getLibraryFile(fileName)
            if (!libraryFile.exists()) {
                Log.w(TAG, "Library file not found: ${libraryFile.absolutePath}")
                return@withContext null
            }

            val jsonString = libraryFile.bufferedReader().use { it.readText() }
            val wordType = object : TypeToken<List<com.fragmentwords.model.Word>>() {}.type
            val words = gson.fromJson<List<com.fragmentwords.model.Word>>(jsonString, wordType)
            Log.d(TAG, "Loaded ${words.size} words from local: $fileName")
            return@withContext words
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local library: $fileName", e)
            return@withContext null
        }
    }

    /**
     * 加载词库词汇
     */
    suspend fun loadLibraryWords(library: LibraryInfo): List<com.fragmentwords.model.Word>? {
        return if (library.isBuiltIn) {
            loadBuiltInLibrary(library.fileName)
        } else {
            loadLocalLibrary(library.fileName)
        }
    }

    /**
     * 获取缓存的词库列表
     */
    private fun getCachedLibraries(): List<LibraryInfo> {
        val json = prefs.getString(KEY_LIBRARY_LIST, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<LibraryInfo>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing cached libraries", e)
            emptyList()
        }
    }

    /**
     * 保存词库列表
     */
    private fun saveLibraries(libraries: List<LibraryInfo>) {
        val json = gson.toJson(libraries)
        prefs.edit().putString(KEY_LIBRARY_LIST, json).apply()
        Log.d(TAG, "Saved ${libraries.size} libraries")
    }

    private fun mergeWithBuiltInLibraries(cachedLibraries: List<LibraryInfo>): List<LibraryInfo> {
        val cachedById = cachedLibraries.associateBy { normalizeLibraryId(it.id) }
        val mergedLibraries = LibraryInfo.BUILT_IN_LIBRARIES.map { builtInLibrary ->
            val cachedLibrary = cachedById[normalizeLibraryId(builtInLibrary.id)] ?: return@map builtInLibrary
            builtInLibrary.copy(
                size = cachedLibrary.size.takeIf { it > 0 } ?: builtInLibrary.size,
                wordCount = cachedLibrary.wordCount.takeIf { it > 0 } ?: builtInLibrary.wordCount,
                isDownloaded = if (builtInLibrary.isBuiltIn) true else cachedLibrary.isDownloaded,
                isEnabled = cachedLibrary.isEnabled
            )
        }

        val mergedById = mergedLibraries
            .map { normalizeLibraryId(it.id) }
            .toSet()
        val extraLibraries = cachedLibraries.filter { normalizeLibraryId(it.id) !in mergedById }
        return mergedLibraries + extraLibraries
    }

    private fun normalizeLibraryId(libraryId: String): String {
        return if (libraryId.equals("grad", ignoreCase = true)) {
            "advanced"
        } else {
            libraryId.lowercase()
        }
    }

    /**
     * 获取词库统计信息
     */
    suspend fun getLibraryStats(): LibraryStats = withContext(Dispatchers.IO) {
        val libraries = getAllLibraries()
        val downloaded = libraries.count { it.isDownloaded }
        val enabled = libraries.count { it.isEnabled && it.isDownloaded }
        val totalWords = libraries.filter { it.isDownloaded }.sumOf { it.wordCount }

        return@withContext LibraryStats(
            totalLibraries = libraries.size,
            downloadedLibraries = downloaded,
            enabledLibraries = enabled,
            totalWords = totalWords
        )
    }

    /**
     * 词库统计数据
     */
    data class LibraryStats(
        val totalLibraries: Int,        // 总词库数
        val downloadedLibraries: Int,  // 已下载词库数
        val enabledLibraries: Int,      // 已启用词库数
        val totalWords: Int             // 总词汇数
    )
}
