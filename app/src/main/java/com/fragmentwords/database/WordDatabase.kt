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
        private const val DATABASE_VERSION = 3  // 升级版本号

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
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_WORDS)
        db.execSQL(CREATE_TABLE_NOTEBOOK)
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
     * 根据单词获取完整信息
     */
    private fun getWordByName(word: String): Word? {
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

        // 检查是否已存在
        if (isInNotebook(word.word)) {
            Log.d(TAG, "Word '${word.word}' already exists in notebook, skipping")
            return -1
        }

        val values = ContentValues().apply {
            put("word", word.word)
            put("phonetic", word.phonetic)
            put("translation", word.translation)
            put("example", word.example)
            put("difficulty", word.difficulty)
            put("part_of_speech", word.partOfSpeech)
            put("library", word.library)
        }
        val result = db.insertWithOnConflict(TABLE_NOTEBOOK, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        if (result != -1L) {
            Log.d(TAG, "Added word '${word.word}' to notebook")
        }
        return result
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
}
