package com.fragmentwords.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.fragmentwords.model.Word

/**
 * 单词数据库管理
 */
class WordDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "WordDatabase"
        private const val DATABASE_NAME = "words.db"
        private const val DATABASE_VERSION = 4  // 升级版本号以支持艾宾浩斯算法

        // 单词表
        private const val TABLE_WORDS = "words"
        private const val CREATE_TABLE_WORDS = """
            CREATE TABLE $TABLE_WORDS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                word TEXT NOT NULL UNIQUE,
                phonetic TEXT,
                translation TEXT,
                example TEXT,
                difficulty INTEGER DEFAULT 1,
                part_of_speech TEXT DEFAULT '',
                library TEXT DEFAULT ''
            )
        """

        // 生词本表
        private const val TABLE_NOTEBOOK = "notebook"
        private const val CREATE_TABLE_NOTEBOOK = """
            CREATE TABLE $TABLE_NOTEBOOK (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                word TEXT NOT NULL,
                phonetic TEXT,
                translation TEXT,
                example TEXT,
                difficulty INTEGER DEFAULT 1,
                part_of_speech TEXT DEFAULT '',
                library TEXT DEFAULT '',
                timestamp INTEGER DEFAULT (strftime('%s', 'now') * 1000)
            )
        """

        // 艾宾浩斯学习进度表
        private const val TABLE_LEARNING_PROGRESS = "learning_progress"
        private const val CREATE_TABLE_LEARNING_PROGRESS = """
            CREATE TABLE $TABLE_LEARNING_PROGRESS (
                word TEXT PRIMARY KEY,
                stage INTEGER DEFAULT 0,
                next_review_time INTEGER DEFAULT 0,
                last_review_time INTEGER DEFAULT 0,
                review_count INTEGER DEFAULT 0,
                known_count INTEGER DEFAULT 0,
                unknown_count INTEGER DEFAULT 0,
                created_time INTEGER DEFAULT (strftime('%s', 'now') * 1000)
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_WORDS)
        db.execSQL(CREATE_TABLE_NOTEBOOK)
        db.execSQL(CREATE_TABLE_LEARNING_PROGRESS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // 添加词性字段
            db.execSQL("ALTER TABLE $TABLE_WORDS ADD COLUMN part_of_speech TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_NOTEBOOK ADD COLUMN part_of_speech TEXT DEFAULT ''")
        }
        if (oldVersion < 3) {
            // 添加词库字段
            db.execSQL("ALTER TABLE $TABLE_WORDS ADD COLUMN library TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_NOTEBOOK ADD COLUMN library TEXT DEFAULT ''")
        }
        if (oldVersion < 4) {
            // 创建艾宾浩斯学习进度表
            db.execSQL(CREATE_TABLE_LEARNING_PROGRESS)
        }
    }

    /**
     * 插入单词到词库
     */
    fun insertWord(word: Word): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("word", word.word)
            put("phonetic", word.phonetic)
            put("translation", word.translation)
            put("example", word.example)
            put("difficulty", word.difficulty)
            put("part_of_speech", word.partOfSpeech)
            put("library", word.library)
        }
        return db.insertWithOnConflict(TABLE_WORDS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * 批量插入单词
     */
    fun insertWords(words: List<Word>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            words.forEach { word ->
                val values = ContentValues().apply {
                    put("word", word.word)
                    put("phonetic", word.phonetic)
                    put("translation", word.translation)
                    put("example", word.example)
                    put("difficulty", word.difficulty)
                    put("part_of_speech", word.partOfSpeech)
                    put("library", word.library)
                }
                db.insertWithOnConflict(TABLE_WORDS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * 获取随机单词
     */
    fun getRandomWord(excludeWord: String? = null): Word? {
        val db = readableDatabase
        if (excludeWord != null) {
            // 先尝试排除当前单词
            val excludeCursor = db.rawQuery(
                "SELECT * FROM $TABLE_WORDS WHERE word != ? ORDER BY RANDOM() LIMIT 1",
                arrayOf(excludeWord)
            )
            excludeCursor.use {
                if (it.moveToFirst()) {
                    return cursorToWord(it)
                }
            }
            // 如果排除后没有其他单词，返回当前单词
            Log.w(TAG, "Only one word available, returning current word")
            return getWordByName(excludeWord)
        } else {
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_WORDS ORDER BY RANDOM() LIMIT 1",
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    return cursorToWord(it)
                }
            }
            return null
        }
    }

    /**
     * 根据词库列表获取随机单词
     */
    fun getRandomWordByLibraries(libraries: List<String>, excludeWord: String? = null): Word? {
        if (libraries.isEmpty()) {
            return getRandomWord(excludeWord)
        }

        val db = readableDatabase
        val placeholders = libraries.joinToString(",") { "?" }

        if (excludeWord != null) {
            // 先尝试排除当前单词
            val excludeCursor = db.rawQuery(
                "SELECT * FROM $TABLE_WORDS WHERE library IN ($placeholders) AND word != ? ORDER BY RANDOM() LIMIT 1",
                libraries.toMutableList().apply { add(excludeWord) }.toTypedArray()
            )
            excludeCursor.use {
                if (it.moveToFirst()) {
                    return cursorToWord(it)
                }
            }
            // 如果排除后没有其他单词，返回 null
            Log.w(TAG, "No other words available in selected libraries")
            return null
        } else {
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_WORDS WHERE library IN ($placeholders) ORDER BY RANDOM() LIMIT 1",
                libraries.toTypedArray()
            )
            cursor.use {
                if (it.moveToFirst()) {
                    return cursorToWord(it)
                }
            }
            return null
        }
    }

    /**
     * 根据单词获取完整信息（公共方法）
     */
    fun getWordByName(word: String): Word? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_WORDS,
            null,
            "word = ?",
            arrayOf(word),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                cursorToWord(it)
            } else {
                null
            }
        }
    }

    /**
     * 根据单词获取完整信息（私有方法，已废弃）
     */
    private fun getWordByNamePrivate(word: String): Word? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_WORDS,
            null,
            "word = ?",
            arrayOf(word),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                cursorToWord(it)
            } else {
                null
            }
        }
    }

    /**
     * 添加单词到生词本（如果不存在则添加，存在则忽略）
     */
    fun addToNotebook(word: Word): Long {
        val db = writableDatabase

        Log.d(TAG, "========== addToNotebook 开始 ==========")
        Log.d(TAG, "单词: ${word.word}")
        Log.d(TAG, "音标: ${word.phonetic}")
        Log.d(TAG, "释义: ${word.translation}")
        Log.d(TAG, "例句: ${word.example}")
        Log.d(TAG, "难度: ${word.difficulty}")
        Log.d(TAG, "词性: ${word.partOfSpeech}")
        Log.d(TAG, "词库: ${word.library}")

        try {
            // 检查是否已存在
            val exists = isInNotebook(word.word)
            Log.d(TAG, "是否已存在: $exists")

            if (exists) {
                Log.d(TAG, "单词'${word.word}'已在生词本中，跳过")
                return -1
            }

            // 构建数据
            val values = ContentValues().apply {
                put("word", word.word)
                put("phonetic", word.phonetic)
                put("translation", word.translation)
                put("example", word.example)
                put("difficulty", word.difficulty)
                put("part_of_speech", word.partOfSpeech)
                put("library", word.library)
            }

            Log.d(TAG, "ContentValues构建完成: ${values.size()} 个字段")

            // 检查必填字段
            if (word.word.isEmpty()) {
                Log.e(TAG, "❌ 单词为空，无法添加！")
                return -1
            }

            // 插入数据库
            Log.d(TAG, "开始插入数据库...")
            val result = db.insert(TABLE_NOTEBOOK, null, values)

            if (result != -1L) {
                Log.d(TAG, "✅ 成功插入数据库，ID: $result")
            } else {
                Log.e(TAG, "❌ 插入数据库失败！result = $result")
            }

            // 立即验证是否真的添加成功
            val verifyExists = isInNotebook(word.word)
            Log.d(TAG, "验证结果: 单词在生词本中 = $verifyExists")

            if (!verifyExists) {
                Log.e(TAG, "❌ 验证失败：单词不在生词本中！")
            }

            Log.d(TAG, "========== addToNotebook 结束 ==========")

            return result

        } catch (e: Exception) {
            Log.e(TAG, "❌ addToNotebook异常: ${e.message}")
            e.printStackTrace()
            Log.e(TAG, "异常堆栈: ${e.stackTraceToString()}")
            return -1
        }
    }

    /**
     * 获取生词本所有单词
     */
    fun getNotebookWords(): List<Word> {
        val db = readableDatabase
        val words = mutableListOf<Word>()
        val cursor = db.query(
            TABLE_NOTEBOOK,
            null,
            null,
            null,
            null,
            null,
            "timestamp DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                words.add(cursorToWord(it))
            }
        }
        return words
    }

    /**
     * 从生词本删除单词
     */
    fun removeFromNotebook(word: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_NOTEBOOK, "word = ?", arrayOf(word))
    }

    /**
     * 清空生词本
     */
    fun clearNotebook(): Int {
        val db = writableDatabase
        return db.delete(TABLE_NOTEBOOK, null, null)
    }

    /**
     * 获取生词本单词数量
     */
    fun getNotebookCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NOTEBOOK", null)
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }
        return 0
    }

    /**
     * 检查单词是否在生词本
     */
    fun isInNotebook(word: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTEBOOK,
            arrayOf("id"),
            "word = ?",
            arrayOf(word),
            null,
            null,
            null
        )
        return cursor.use { it.count > 0 }
    }

    /**
     * 获取词库单词总数
     */
    fun getWordCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_WORDS", null)
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }
        return 0
    }

    /**
     * 根据词库列表获取单词总数
     */
    fun getWordCountByLibraries(libraries: List<String>): Int {
        if (libraries.isEmpty()) {
            return getWordCount()
        }

        val db = readableDatabase
        val placeholders = libraries.joinToString(",") { "?" }
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_WORDS WHERE library IN ($placeholders)",
            libraries.toTypedArray()
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }
        return 0
    }

    /**
     * 获取单个词库的单词总数
     */
    fun getWordCountByLibrary(library: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_WORDS WHERE library = ?",
            arrayOf(library)
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }
        return 0
    }

    /**
     * 删除指定词库的所有单词
     */
    fun deleteWordsByLibrary(library: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_WORDS, "library = ?", arrayOf(library))
    }

    private fun cursorToWord(cursor: Cursor): Word {
        return Word(
            word = cursor.getString(cursor.getColumnIndexOrThrow("word")),
            phonetic = cursor.getString(cursor.getColumnIndexOrThrow("phonetic")) ?: "",
            translation = cursor.getString(cursor.getColumnIndexOrThrow("translation")),
            example = cursor.getString(cursor.getColumnIndexOrThrow("example")),
            difficulty = cursor.getInt(cursor.getColumnIndexOrThrow("difficulty")),
            partOfSpeech = cursor.getString(cursor.getColumnIndexOrThrow("part_of_speech")) ?: "",
            library = cursor.getString(cursor.getColumnIndexOrThrow("library")) ?: ""
        )
    }

    // ==================== 艾宾浩斯学习进度相关方法 ====================

    /**
     * 获取单词的学习进度
     */
    fun getLearningProgress(word: String): LearningProgress? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LEARNING_PROGRESS,
            null,
            "word = ?",
            arrayOf(word),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                LearningProgress(
                    word = it.getString(it.getColumnIndexOrThrow("word")),
                    stage = it.getInt(it.getColumnIndexOrThrow("stage")),
                    nextReviewTime = it.getLong(it.getColumnIndexOrThrow("next_review_time")),
                    lastReviewTime = it.getLong(it.getColumnIndexOrThrow("last_review_time")),
                    reviewCount = it.getInt(it.getColumnIndexOrThrow("review_count")),
                    knownCount = it.getInt(it.getColumnIndexOrThrow("known_count")),
                    unknownCount = it.getInt(it.getColumnIndexOrThrow("unknown_count")),
                    createdTime = it.getLong(it.getColumnIndexOrThrow("created_time"))
                )
            } else {
                null
            }
        }
    }

    /**
     * 更新或插入学习进度
     */
    fun updateLearningProgress(progress: LearningProgress): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("word", progress.word)
            put("stage", progress.stage)
            put("next_review_time", progress.nextReviewTime)
            put("last_review_time", progress.lastReviewTime)
            put("review_count", progress.reviewCount)
            put("known_count", progress.knownCount)
            put("unknown_count", progress.unknownCount)
            put("created_time", progress.createdTime)
        }
        return db.insertWithOnConflict(
            TABLE_LEARNING_PROGRESS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * 获取所有需要复习的单词
     */
    fun getWordsToReview(limit: Int = 20): List<String> {
        val db = readableDatabase
        val now = System.currentTimeMillis()
        val words = mutableListOf<String>()

        val cursor = db.rawQuery(
            """SELECT word FROM $TABLE_LEARNING_PROGRESS
                WHERE next_review_time <= ? AND stage < 8
                ORDER BY next_review_time ASC
                LIMIT ?""",
            arrayOf(now.toString(), limit.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                words.add(it.getString(it.getColumnIndexOrThrow("word")))
            }
        }
        return words
    }

    /**
     * 获取需要复习的单词（限制词库）
     */
    fun getWordsToReviewByLibraries(libraries: List<String>, limit: Int = 20): List<String> {
        if (libraries.isEmpty()) {
            return getWordsToReview(limit)
        }

        val db = readableDatabase
        val now = System.currentTimeMillis()
        val words = mutableListOf<String>()
        val placeholders = libraries.joinToString(",") { "?" }

        val cursor = db.rawQuery(
            """SELECT p.word FROM $TABLE_LEARNING_PROGRESS p
                INNER JOIN $TABLE_WORDS w ON p.word = w.word
                WHERE p.next_review_time <= ? AND p.stage < 8 AND w.library IN ($placeholders)
                ORDER BY p.next_review_time ASC
                LIMIT ?""",
            libraries.toMutableList().apply {
                add(0, now.toString())
                add(limit.toString())
            }.toTypedArray()
        )

        cursor.use {
            while (it.moveToNext()) {
                words.add(it.getString(it.getColumnIndexOrThrow("word")))
            }
        }
        return words
    }

    /**
     * 获取学习统计数据
     */
    fun getLearningStats(): LearningStats {
        val db = readableDatabase
        val now = System.currentTimeMillis()

        // 总学习单词数
        val totalCursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LEARNING_PROGRESS",
            null
        )
        val total = totalCursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

        // 已掌握单词数（stage >= 8）
        val masteredCursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LEARNING_PROGRESS WHERE stage >= 8",
            null
        )
        val mastered = masteredCursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

        // 待复习单词数
        val needReviewCursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LEARNING_PROGRESS WHERE next_review_time <= ? AND stage < 8",
            arrayOf(now.toString())
        )
        val needReview = needReviewCursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

        // 新单词数（stage = 0）
        val newCursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LEARNING_PROGRESS WHERE stage = 0",
            null
        )
        val new = newCursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

        val masteryRate = if (total > 0) (mastered * 100 / total) else 0

        return LearningStats(
            totalWords = total,
            masteredWords = mastered,
            needReviewWords = needReview,
            newWords = new,
            masteryRate = masteryRate
        )
    }

    /**
     * 清除所有学习进度
     */
    fun clearAllProgress(): Int {
        val db = writableDatabase
        return db.delete(TABLE_LEARNING_PROGRESS, null, null)
    }

    /**
     * 学习进度数据类
     */
    data class LearningProgress(
        val word: String,
        val stage: Int = 0,                    // 当前复习阶段（0-8）
        val nextReviewTime: Long = 0,          // 下次复习时间戳
        val lastReviewTime: Long = 0,          // 上次复习时间戳
        val reviewCount: Int = 0,              // 总复习次数
        val knownCount: Int = 0,               // 认识次数
        val unknownCount: Int = 0,             // 不认识次数
        val createdTime: Long = 0              // 创建时间
    )

    /**
     * 学习统计数据类
     */
    data class LearningStats(
        val totalWords: Int,        // 总学习单词数
        val masteredWords: Int,     // 已掌握单词数
        val needReviewWords: Int,   // 待复习单词数
        val newWords: Int,          // 新单词数
        val masteryRate: Int        // 掌握率（百分比）
    )
}
