package com.fragmentwords.data

import android.content.Context
import android.util.Log
import com.fragmentwords.database.WordDatabase
import com.fragmentwords.manager.LibraryManager
import com.fragmentwords.model.Word
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

/**
 * 单词数据仓库
 */
class WordRepository(context: Context) {

    private val context = context
    private val database = WordDatabase(context)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val libraryManager = LibraryManager(context)

    companion object {
        private const val PREFS_NAME = "word_prefs"
        private const val KEY_INITIALIZED = "initialized"
        private const val KEY_SELECTED_LIBRARIES = "selected_libraries"
    }

    /**
     * 初始化词库数据
     */
    suspend fun initializeIfNeeded() {
        if (!isInitialized()) {
            loadDefaultWords()
            markInitialized()
        }
    }

    /**
     * 从 assets 加载默认单词数据
     */
    private fun loadDefaultWords() {
        try {
            var totalLoaded = 0

            // 从JSON文件加载CET4词库（多个文件）
            val cet4Files = listOf(
                "data/cet4_words.json",
                "data/cet4_words_part2.json"
                // 可以继续添加更多文件
                // "data/cet4_words_part3.json",
                // "data/cet4_words_part4.json",
                // "data/cet4_words_part5.json"
            )

            cet4Files.forEach { fileName ->
                val words = loadWordsFromJson(fileName)
                if (words.isNotEmpty()) {
                    database.insertWords(words)
                    totalLoaded += words.size
                    Log.d("WordRepository", "Loaded ${words.size} words from $fileName")
                }
            }

            // 可以继续添加其他词库
            // val cet6Files = listOf("data/cet6_words.json", "data/cet6_words_part2.json")
            // val ieltsFiles = listOf("data/ielts_words.json", "data/ielts_words_part2.json")
            // val toeflFiles = listOf("data/toefl_words.json", "data/toefl_words_part2.json")

            Log.d("WordRepository", "Total words loaded: $totalLoaded")

            // 如果没有加载到任何单词，使用代码中的默认词库
            if (totalLoaded == 0) {
                Log.w("WordRepository", "No words loaded from JSON, using default words")
                val defaultWords = getAllDefaultWords()
                database.insertWords(defaultWords)
            }

        } catch (e: Exception) {
            Log.e("WordRepository", "Error loading words from JSON: ${e.message}")
            // 如果JSON加载失败，使用代码中的默认词库
            val defaultWords = getAllDefaultWords()
            database.insertWords(defaultWords)
        }
    }

    /**
     * 从JSON文件加载单词
     */
    private fun loadWordsFromJson(path: String): List<Word> {
        try {
            val inputStream = context.assets.open(path)
            val reader = InputStreamReader(inputStream)
            val wordType = object : TypeToken<List<Word>>() {}.type
            val words = Gson().fromJson<List<Word>>(reader, wordType)
            reader.close()
            inputStream.close()
            return words ?: emptyList()
        } catch (e: Exception) {
            Log.e("WordRepository", "Error loading JSON from $path: ${e.message}")
            return emptyList()
        }
    }

    /**
     * 获取下一个单词（从启用的词库中）
     */
    suspend fun getNextWord(excludeWord: String? = null): Word? {
        // 从LibraryManager获取已启用的词库ID列表
        val enabledLibraryIds = libraryManager.getEnabledLibraryIds()
        return if (enabledLibraryIds.isNotEmpty()) {
            // 将词库ID转换为大写作为数据库的library字段
            val libraryNames = enabledLibraryIds.map { it.uppercase() }
            database.getRandomWordByLibraries(libraryNames, excludeWord)
        } else {
            // 如果没有启用任何词库，返回null
            Log.w("WordRepository", "No libraries enabled")
            null
        }
    }

    /**
     * 同步获取下一个单词（用于兼容非协程上下文）
     */
    fun getNextWordSync(excludeWord: String? = null): Word? {
        // 同步读取词库选择
        val json = prefs.getString(KEY_SELECTED_LIBRARIES, null)
        val selectedLibraries = if (json != null) {
            try {
                Gson().fromJson(json, object : TypeToken<List<String>>() {}.type) ?: listOf("CET4")
            } catch (e: Exception) {
                listOf("CET4")
            }
        } else {
            listOf("CET4")
        }

        // 根据选择的词库获取单词
        return if (selectedLibraries.isNotEmpty()) {
            database.getRandomWordByLibraries(selectedLibraries, excludeWord)
        } else {
            database.getRandomWord(excludeWord)
        }
    }

    /**
     * 获取当前正在学习的单词（从SharedPreferences获取）
     */
    fun getCurrentWord(): Word? {
        val wordJson = prefs.getString("current_word", null) ?: return null
        return try {
            Gson().fromJson(wordJson, Word::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 保存当前正在学习的单词
     */
    fun saveCurrentWord(word: Word) {
        val wordJson = Gson().toJson(word)
        prefs.edit().putString("current_word", wordJson).apply()
    }

    /**
     * 清除当前单词
     */
    fun clearCurrentWord() {
        prefs.edit().remove("current_word").apply()
    }

    /**
     * 添加到生词本
     */
    fun addToNotebook(word: Word): Boolean {
        val result = database.addToNotebook(word)
        return result > 0
    }

    /**
     * 获取生词本
     */
    fun getNotebookWords(): List<Word> {
        return database.getNotebookWords()
    }

    /**
     * 从生词本删除
     */
    fun removeFromNotebook(word: String): Int {
        return database.removeFromNotebook(word)
    }

    /**
     * 清空生词本
     */
    fun clearNotebook(): Int {
        return database.clearNotebook()
    }

    /**
     * 获取生词本数量
     */
    fun getNotebookCount(): Int {
        return database.getNotebookCount()
    }

    /**
     * 获取词库单词总数
     */
    fun getWordCount(): Int {
        return database.getWordCount()
    }

    /**
     * 获取已选择的词库列表
     */
    suspend fun getSelectedLibraries(): List<String> {
        val json = prefs.getString(KEY_SELECTED_LIBRARIES, null) ?: return listOf("CET4")
        return try {
            Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) {
            listOf("CET4")
        }
    }

    /**
     * 保存已选择的词库列表
     */
    suspend fun saveSelectedLibraries(libraries: List<String>) {
        val json = Gson().toJson(libraries)
        prefs.edit().putString(KEY_SELECTED_LIBRARIES, json).apply()
    }

    /**
     * 动态加载词库到数据库
     * @param libraryId 词库ID (如 "cet4", "cet6", "ielts")
     * @return 是否加载成功
     */
    suspend fun loadLibraryToDatabase(libraryId: String): Boolean {
        return try {
            Log.d("WordRepository", "Loading library: $libraryId")

            // 从LibraryManager获取词库信息
            val libraries = libraryManager.getAllLibraries()
            val library = libraries.find { it.id == libraryId }

            if (library == null) {
                Log.e("WordRepository", "Library not found: $libraryId")
                return false
            }

            // 使用LibraryManager加载词库词汇
            val words = libraryManager.loadLibraryWords(library)

            if (words.isNullOrEmpty()) {
                Log.e("WordRepository", "No words loaded from library: $libraryId")
                return false
            }

            // 插入到数据库
            val inserted = database.insertWords(words)
            Log.d("WordRepository", "Inserted $inserted words from library: $libraryId")

            true
        } catch (e: Exception) {
            Log.e("WordRepository", "Error loading library $libraryId", e)
            false
        }
    }

    /**
     * 从数据库删除词库的所有单词
     * @param libraryId 词库ID
     * @return 是否删除成功
     */
    suspend fun deleteLibraryFromDatabase(libraryId: String): Boolean {
        return try {
            val libraryName = libraryId.uppercase()
            val deleted = database.deleteWordsByLibrary(libraryName)
            Log.d("WordRepository", "Deleted $deleted words from library: $libraryId")
            deleted > 0
        } catch (e: Exception) {
            Log.e("WordRepository", "Error deleting library $libraryId", e)
            false
        }
    }

    /**
     * 检查词库是否已加载到数据库
     * @param libraryId 词库ID
     * @return 是否已加载
     */
    suspend fun isLibraryLoaded(libraryId: String): Boolean {
        return try {
            val libraryName = libraryId.uppercase()
            val count = database.getWordCountByLibrary(libraryName)
            count > 0
        } catch (e: Exception) {
            Log.e("WordRepository", "Error checking library $libraryId", e)
            false
        }
    }

    private fun isInitialized(): Boolean {
        return prefs.getBoolean(KEY_INITIALIZED, false)
    }

    private fun markInitialized() {
        prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
    }

    /**
     * 获取所有默认单词数据（包含多个词库）
     */
    private fun getAllDefaultWords(): List<Word> {
        return listOf(
            // ========== CET4 四级词汇 ==========
            Word(
                word = "abandon",
                phonetic = "/əˈbændən/",
                translation = "v. 抛弃，舍弃，放弃",
                example = "We had to abandon the car and walk.",
                difficulty = 2,
                partOfSpeech = "v.",
                library = "CET4"
            ),
            Word(
                word = "ability",
                phonetic = "/əˈbɪləti/",
                translation = "n. 能力，本领",
                example = "He has the ability to solve problems.",
                difficulty = 2,
                partOfSpeech = "n.",
                library = "CET4"
            ),
            Word(
                word = "absolute",
                phonetic = "/ˈæbsəluːt/",
                translation = "adj. 绝对的，完全的",
                example = "I have absolute confidence in her.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "CET4"
            ),
            Word(
                word = "academic",
                phonetic = "/ˌækəˈdemɪk/",
                translation = "adj. 学术的，学院的",
                example = "The academic year begins in September.",
                difficulty = 2,
                partOfSpeech = "adj.",
                library = "CET4"
            ),
            Word(
                word = "achieve",
                phonetic = "/əˈtʃiːv/",
                translation = "v. 实现，达到",
                example = "You will achieve your goal if you work hard.",
                difficulty = 2,
                partOfSpeech = "v.",
                library = "CET4"
            ),
            Word(
                word = "benefit",
                phonetic = "/ˈbenɪfɪt/",
                translation = "n. 利益，好处 v. 有益于",
                example = "The new policy will benefit many people.",
                difficulty = 2,
                partOfSpeech = "n./v.",
                library = "CET4"
            ),
            Word(
                word = "capture",
                phonetic = "/ˈkæptʃər/",
                translation = "v. 捕获，占领",
                example = "The police captured the thief.",
                difficulty = 2,
                partOfSpeech = "v.",
                library = "CET4"
            ),
            Word(
                word = "decade",
                phonetic = "/ˈdekeɪd/",
                translation = "n. 十年",
                example = "The bridge was built a decade ago.",
                difficulty = 2,
                partOfSpeech = "n.",
                library = "CET4"
            ),

            // ========== CET6 六级词汇 ==========
            Word(
                word = "ambiguous",
                phonetic = "/æmˈbɪɡjuəs/",
                translation = "adj. 模棱两可的，含糊不清的",
                example = "The instructions were ambiguous and confusing.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "CET6"
            ),
            Word(
                word = "coincide",
                phonetic = "/ˌkəʊɪnˈsaɪd/",
                translation = "v. 同时发生；相符",
                example = "His arrival coincided with our departure.",
                difficulty = 4,
                partOfSpeech = "v.",
                library = "CET6"
            ),
            Word(
                word = "compatible",
                phonetic = "/kəmˈpætəbl/",
                translation = "adj. 兼容的，能共处的",
                example = "This software is compatible with most systems.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "CET6"
            ),
            Word(
                word = "dilemma",
                phonetic = "/dɪˈlemə/",
                translation = "n. 困境，进退两难",
                example = "She faced a dilemma: stay or leave.",
                difficulty = 4,
                partOfSpeech = "n.",
                library = "CET6"
            ),
            Word(
                word = "ethnic",
                phonetic = "/ˈeθnɪk/",
                translation = "adj. 种族的，部落的",
                example = "The city has a diverse ethnic population.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "CET6"
            ),
            Word(
                word = "heterogeneous",
                phonetic = "/ˌhetərəˈdʒiːniəs/",
                translation = "adj. 异类的，不同的",
                example = "The population is heterogeneous in culture.",
                difficulty = 5,
                partOfSpeech = "adj.",
                library = "CET6"
            ),
            Word(
                word = "intrinsic",
                phonetic = "/ɪnˈtrɪnzɪk/",
                translation = "adj. 固有的，内在的",
                example = "Math is intrinsic to computer science.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "CET6"
            ),

            // ========== IELTS 雅思词汇 ==========
            Word(
                word = "accommodate",
                phonetic = "/əˈkɒmədeɪt/",
                translation = "v. 容纳；使适应",
                example = "The hall can accommodate 500 people.",
                difficulty = 4,
                partOfSpeech = "v.",
                library = "IELTS"
            ),
            Word(
                word = "constitute",
                phonetic = "/ˈkɒnstɪtjuːt/",
                translation = "v. 构成，组成",
                example = "Women constitute 50% of the population.",
                difficulty = 4,
                partOfSpeech = "v.",
                library = "IELTS"
            ),
            Word(
                word = "deteriorate",
                phonetic = "/dɪˈtɪəriəreɪt/",
                translation = "v. 恶化，变坏",
                example = "Air quality has deteriorated in the city.",
                difficulty = 5,
                partOfSpeech = "v.",
                library = "IELTS"
            ),
            Word(
                word = "enhance",
                phonetic = "/ɪnˈhɑːns/",
                translation = "v. 提高，增强",
                example = "Good lighting can enhance the room.",
                difficulty = 4,
                partOfSpeech = "v.",
                library = "IELTS"
            ),
            Word(
                word = "fluctuate",
                phonetic = "/ˈflʌktʃueɪt/",
                translation = "v. 波动，起伏",
                example = "Prices fluctuate according to demand.",
                difficulty = 4,
                partOfSpeech = "v.",
                library = "IELTS"
            ),
            Word(
                word = "hypothesis",
                phonetic = "/haɪˈpɒθəsɪs/",
                translation = "n. 假设，假说",
                example = "We need to test this hypothesis experimentally.",
                difficulty = 4,
                partOfSpeech = "n.",
                library = "IELTS"
            ),
            Word(
                word = "implement",
                phonetic = "/ˈɪmplɪment/",
                translation = "v. 实施，执行",
                example = "The government will implement the new policy.",
                difficulty = 3,
                partOfSpeech = "v.",
                library = "IELTS"
            ),
            Word(
                word = "perspective",
                phonetic = "/pəˈspektɪv/",
                translation = "n. 视角，观点",
                example = "Try to see it from my perspective.",
                difficulty = 3,
                partOfSpeech = "n.",
                library = "IELTS"
            ),

            // ========== TOEFL 托福词汇 ==========
            Word(
                word = "accumulate",
                phonetic = "/əˈkjuːmjəleɪt/",
                translation = "v. 积累，积聚",
                example = "Dust accumulates quickly in this room.",
                difficulty = 4,
                partOfSpeech = "v.",
                library = "TOEFL"
            ),
            Word(
                word = "advocate",
                phonetic = "/ˈædvəkeɪt/",
                translation = "v. 提倡，主张",
                example = "Many doctors advocate a healthy diet.",
                difficulty = 4,
                partOfSpeech = "v.",
                library = "TOEFL"
            ),
            Word(
                word = "conspicuous",
                phonetic = "/kənˈspɪkjuəs/",
                translation = "adj. 显眼的，明显的",
                example = "She felt conspicuous in her bright red dress.",
                difficulty = 5,
                partOfSpeech = "adj.",
                library = "TOEFL"
            ),
            Word(
                word = "derive",
                phonetic = "/dɪˈraɪv/",
                translation = "v. 获得，源于",
                example = "Many English words derive from Latin.",
                difficulty = 4,
                partOfSpeech = "v.",
                library = "TOEFL"
            ),
            Word(
                word = "eccentric",
                phonetic = "/ɪkˈsentrɪk/",
                translation = "adj. 古怪的，异乎寻常的",
                example = "The eccentric artist lived in a house made of bottles.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "TOEFL"
            ),
            Word(
                word = "inherent",
                phonetic = "/ɪnˈhɪərənt/",
                translation = "adj. 固有的，内在的",
                example = "Risks are inherent in the investment.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "TOEFL"
            ),
            Word(
                word = "manifest",
                phonetic = "/ˈmænɪfest/",
                translation = "v. 显示，表明",
                example = "The symptoms manifested within days.",
                difficulty = 5,
                partOfSpeech = "v.",
                library = "TOEFL"
            ),
            Word(
                word = "plausible",
                phonetic = "/ˈplɔːzəbl/",
                translation = "adj. 貌似可信的",
                example = "His explanation seems plausible.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "TOEFL"
            ),

            // ========== GRE 词汇 ==========
            Word(
                word = "aberration",
                phonetic = "/ˌæbəˈreɪʃn/",
                translation = "n. 偏差，异常",
                example = "The scientist noted an aberration in the data.",
                difficulty = 5,
                partOfSpeech = "n.",
                library = "GRE"
            ),
            Word(
                word = "candid",
                phonetic = "/ˈkændɪd/",
                translation = "adj. 坦率的，直言不讳的",
                example = "She gave a candid assessment of the situation.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "GRE"
            ),
            Word(
                word = "dichotomy",
                phonetic = "/daɪˈkɒtəmi/",
                translation = "n. 二分法，对立",
                example = "There is a dichotomy between theory and practice.",
                difficulty = 5,
                partOfSpeech = "n.",
                library = "GRE"
            ),
            Word(
                word = "enigmatic",
                phonetic = "/ˌenɪɡˈmætɪk/",
                translation = "adj. 神秘的，难以理解的",
                example = "She smiled an enigmatic smile.",
                difficulty = 5,
                partOfSpeech = "adj.",
                library = "GRE"
            ),
            Word(
                word = "fastidious",
                phonetic = "/fæˈstɪdiəs/",
                translation = "adj. 挑剔的，难以取悦的",
                example = "She is fastidious about her food.",
                difficulty = 5,
                partOfSpeech = "adj.",
                library = "GRE"
            ),
            Word(
                word = "juxtapose",
                phonetic = "/ˌdʒʌkstəˈpəʊz/",
                translation = "v. 并列，并置",
                example = "The artist juxtaposes light and shadow in her paintings.",
                difficulty = 5,
                partOfSpeech = "v.",
                library = "GRE"
            ),
            Word(
                word = "laconic",
                phonetic = "/ləˈkɒnɪk/",
                translation = "adj. 简洁的，寡言的",
                example = "His laconic reply ended the conversation.",
                difficulty = 5,
                partOfSpeech = "adj.",
                library = "GRE"
            ),
            Word(
                word = "querulous",
                phonetic = "/ˈkwerələs/",
                translation = "adj. 爱抱怨的，易怒的",
                example = "The querulous customer demanded to see the manager.",
                difficulty = 5,
                partOfSpeech = "adj.",
                library = "GRE"
            ),

            // ========== ADVANCED 高级词汇 ==========
            Word(
                word = "ambience",
                phonetic = "/ˈæmbiəns/",
                translation = "n. 气氛，氛围，情调",
                example = "The restaurant has a pleasant ambience.",
                difficulty = 3,
                partOfSpeech = "n.",
                library = "ADVANCED"
            ),
            Word(
                word = "ephemeral",
                phonetic = "/ɪˈfemərəl/",
                translation = "adj. 短暂的，瞬息的",
                example = "Fame is often ephemeral in the entertainment industry.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "serendipity",
                phonetic = "/ˌserənˈdɪpəti/",
                translation = "n. 意外发现珍奇事物的运气",
                example = "Finding that book was pure serendipity.",
                difficulty = 5,
                partOfSpeech = "n.",
                library = "ADVANCED"
            ),
            Word(
                word = "resilient",
                phonetic = "/rɪˈzɪliənt/",
                translation = "adj. 有弹性的，有恢复力的",
                example = "Children are often more resilient than adults.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "eloquent",
                phonetic = "/ˈeləkwənt/",
                translation = "adj. 雄辩的，有口才的",
                example = "She gave an eloquent speech at the conference.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "pragmatic",
                phonetic = "/præɡˈmætɪk/",
                translation = "adj. 务实的，实用主义的",
                example = "We need a pragmatic approach to solve this problem.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "meticulous",
                phonetic = "/məˈtɪkjələs/",
                translation = "adj. 一丝不苟的，小心翼翼的",
                example = "She is meticulous in her work.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "ubiquitous",
                phonetic = "/juːˈbɪkwɪtəs/",
                translation = "adj. 无处不在的，普遍存在的",
                example = "Smartphones have become ubiquitous in modern life.",
                difficulty = 5,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "altruistic",
                phonetic = "/ˌæltruˈɪstɪk/",
                translation = "adj. 利他的，无私心的",
                example = "His altruistic nature led him to volunteer regularly.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "aesthetic",
                phonetic = "/iːsˈθetɪk/",
                translation = "adj. 美学的，审美的",
                example = "The building has great aesthetic appeal.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "candid",
                phonetic = "/ˈkændɪd/",
                translation = "adj. 坦率的，直言不讳的",
                example = "She gave a candid assessment of the situation.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "diligent",
                phonetic = "/ˈdɪlɪdʒənt/",
                translation = "adj. 勤勉的，勤奋的",
                example = "He is a diligent student who always completes his homework.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "empathy",
                phonetic = "/ˈempəθi/",
                translation = "n. 同情，共鸣",
                example = "She showed great empathy toward the victims.",
                difficulty = 3,
                partOfSpeech = "n.",
                library = "ADVANCED"
            ),
            Word(
                word = "fortitude",
                phonetic = "/ˈfɔːrtɪtuːd/",
                translation = "n. 刚毅，坚忍",
                example = "She faced her illness with great fortitude.",
                difficulty = 4,
                partOfSpeech = "n.",
                library = "ADVANCED"
            ),
            Word(
                word = "gregarious",
                phonetic = "/ɡrɪˈɡeəriəs/",
                translation = "adj. 群居的，爱交际的",
                example = "He is a gregarious person who loves parties.",
                difficulty = 5,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "integrity",
                phonetic = "/ɪnˈteɡrəti/",
                translation = "n. 正直，诚实",
                example = "He is a man of absolute integrity.",
                difficulty = 3,
                partOfSpeech = "n.",
                library = "ADVANCED"
            ),
            Word(
                word = "kindle",
                phonetic = "/ˈkɪndl/",
                translation = "v. 点燃，激起",
                example = "Her speech kindled hope in the audience.",
                difficulty = 3,
                partOfSpeech = "v.",
                library = "ADVANCED"
            ),
            Word(
                word = "lucid",
                phonetic = "/ˈluːsɪd/",
                translation = "adj. 清晰的，易懂的",
                example = "She provided a lucid explanation of the complex theory.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "mundane",
                phonetic = "/mʌnˈdeɪn/",
                translation = "adj. 平凡的，世俗的",
                example = "He was tired of his mundane daily routine.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "nostalgia",
                phonetic = "/nɒˈstældʒə/",
                translation = "n. 怀旧，乡愁",
                example = "Looking at old photos filled her with nostalgia.",
                difficulty = 3,
                partOfSpeech = "n.",
                library = "ADVANCED"
            ),
            Word(
                word = "oblivious",
                phonetic = "/əˈblɪviəs/",
                translation = "adj. 未注意的，不知晓的",
                example = "He was oblivious to the danger around him.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "perceptive",
                phonetic = "/pəˈseptɪv/",
                translation = "adj. 敏锐的，有洞察力的",
                example = "She is perceptive about people's feelings.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "quintessential",
                phonetic = "/ˌkwɪntɪˈsenʃl/",
                translation = "adj. 典型的，完美的",
                example = "She is the quintessential modern woman.",
                difficulty = 5,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "resilience",
                phonetic = "/rɪˈzɪliəns/",
                translation = "n. 恢复力，适应力",
                example = "Children show remarkable resilience to change.",
                difficulty = 3,
                partOfSpeech = "n.",
                library = "ADVANCED"
            ),
            Word(
                word = "stoic",
                phonetic = "/ˈstəʊɪk/",
                translation = "adj. 斯多葛派的，坚忍的",
                example = "He remained stoic despite the setbacks.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "tenacious",
                phonetic = "/təˈneɪʃəs/",
                translation = "adj. 顽强的，坚韧的",
                example = "She is a tenacious negotiator.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "unprecedented",
                phonetic = "/ʌnˈpresɪdentɪd/",
                translation = "adj. 前所未有的",
                example = "The crisis reached unprecedented proportions.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "versatile",
                phonetic = "/ˈvɜːsətaɪl/",
                translation = "adj. 多才多艺的，通用的",
                example = "She is a versatile athlete who excels in multiple sports.",
                difficulty = 3,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "whimsical",
                phonetic = "/ˈwɪmzɪkl/",
                translation = "adj. 异想天开的，古怪的",
                example = "His whimsical sense of humor delights everyone.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            ),
            Word(
                word = "zealous",
                phonetic = "/ˈzeləs/",
                translation = "adj. 热心的，热情的",
                example = "She is a zealous advocate for animal rights.",
                difficulty = 4,
                partOfSpeech = "adj.",
                library = "ADVANCED"
            )
        )
    }

    /**
     * 默认单词数据（示例）- 已废弃，使用getAllDefaultWords()
     */
    private fun getDefaultWords(): List<Word> {
        return listOf(
            // 原有10个单词
            Word(
                word = "ambience",
                phonetic = "/ˈæmbiəns/",
                translation = "n. 气氛，氛围，情调",
                example = "The restaurant has a pleasant ambience.",
                difficulty = 3,
                partOfSpeech = "n."
            ),
            Word(
                word = "ephemeral",
                phonetic = "/ɪˈfemərəl/",
                translation = "adj. 短暂的，瞬息的",
                example = "Fame is often ephemeral in the entertainment industry.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "serendipity",
                phonetic = "/ˌserənˈdɪpəti/",
                translation = "n. 意外发现珍奇事物的运气",
                example = "Finding that book was pure serendipity.",
                difficulty = 5,
                partOfSpeech = "n."
            ),
            Word(
                word = "resilient",
                phonetic = "/rɪˈzɪliənt/",
                translation = "adj. 有弹性的，有恢复力的",
                example = "Children are often more resilient than adults.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            Word(
                word = "eloquent",
                phonetic = "/ˈeləkwənt/",
                translation = "adj. 雄辩的，有口才的",
                example = "She gave an eloquent speech at the conference.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            Word(
                word = "pragmatic",
                phonetic = "/præɡˈmætɪk/",
                translation = "adj. 务实的，实用主义的",
                example = "We need a pragmatic approach to solve this problem.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            Word(
                word = "meticulous",
                phonetic = "/məˈtɪkjələs/",
                translation = "adj. 一丝不苟的，小心翼翼的",
                example = "She is meticulous in her work.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "ubiquitous",
                phonetic = "/juːˈbɪkwɪtəs/",
                translation = "adj. 无处不在的，普遍存在的",
                example = "Smartphones have become ubiquitous in modern life.",
                difficulty = 5,
                partOfSpeech = "adj."
            ),
            Word(
                word = "altruistic",
                phonetic = "/ˌæltruˈɪstɪk/",
                translation = "adj. 利他的，无私心的",
                example = "His altruistic nature led him to volunteer regularly.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "aesthetic",
                phonetic = "/iːsˈθetɪk/",
                translation = "adj. 美学的，审美的",
                example = "The building has great aesthetic appeal.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            // 新增30个单词
            Word(
                word = "candid",
                phonetic = "/ˈkændɪd/",
                translation = "adj. 坦率的，直言不讳的",
                example = "She gave a candid assessment of the situation.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            Word(
                word = "diligent",
                phonetic = "/ˈdɪlɪdʒənt/",
                translation = "adj. 勤勉的，勤奋的",
                example = "He is a diligent student who always completes his homework.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            Word(
                word = "empathy",
                phonetic = "/ˈempəθi/",
                translation = "n. 同情，共鸣",
                example = "She showed great empathy toward the victims.",
                difficulty = 3,
                partOfSpeech = "n."
            ),
            Word(
                word = "fortitude",
                phonetic = "/ˈfɔːrtɪtuːd/",
                translation = "n. 刚毅，坚忍",
                example = "She faced her illness with great fortitude.",
                difficulty = 4,
                partOfSpeech = "n."
            ),
            Word(
                word = "gregarious",
                phonetic = "/ɡrɪˈɡeəriəs/",
                translation = "adj. 群居的，爱交际的",
                example = "He is a gregarious person who loves parties.",
                difficulty = 5,
                partOfSpeech = "adj."
            ),
            Word(
                word = "hypothesis",
                phonetic = "/haɪˈpɒθəsɪs/",
                translation = "n. 假设，假说",
                example = "We need to test this hypothesis experimentally.",
                difficulty = 4,
                partOfSpeech = "n."
            ),
            Word(
                word = "integrity",
                phonetic = "/ɪnˈteɡrəti/",
                translation = "n. 正直，诚实",
                example = "He is a man of absolute integrity.",
                difficulty = 3,
                partOfSpeech = "n."
            ),
            Word(
                word = "juxtapose",
                phonetic = "/ˌdʒʌkstəˈpəʊz/",
                translation = "v. 并列，并置",
                example = "The artist juxtaposes light and shadow in her paintings.",
                difficulty = 5,
                partOfSpeech = "v."
            ),
            Word(
                word = "kindle",
                phonetic = "/ˈkɪndl/",
                translation = "v. 点燃，激起",
                example = "Her speech kindled hope in the audience.",
                difficulty = 3,
                partOfSpeech = "v."
            ),
            Word(
                word = "lucid",
                phonetic = "/ˈluːsɪd/",
                translation = "adj. 清晰的，易懂的",
                example = "She provided a lucid explanation of the complex theory.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "mundane",
                phonetic = "/mʌnˈdeɪn/",
                translation = "adj. 平凡的，世俗的",
                example = "He was tired of his mundane daily routine.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            Word(
                word = "nostalgia",
                phonetic = "/nɒˈstældʒə/",
                translation = "n. 怀旧，乡愁",
                example = "Looking at old photos filled her with nostalgia.",
                difficulty = 3,
                partOfSpeech = "n."
            ),
            Word(
                word = "oblivious",
                phonetic = "/əˈblɪviəs/",
                translation = "adj. 未注意的，不知晓的",
                example = "He was oblivious to the danger around him.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "perceptive",
                phonetic = "/pəˈseptɪv/",
                translation = "adj. 敏锐的，有洞察力的",
                example = "She is perceptive about people's feelings.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "quintessential",
                phonetic = "/ˌkwɪntɪˈsenʃl/",
                translation = "adj. 典型的，完美的",
                example = "She is the quintessential modern woman.",
                difficulty = 5,
                partOfSpeech = "adj."
            ),
            Word(
                word = "resilience",
                phonetic = "/rɪˈzɪliəns/",
                translation = "n. 恢复力，适应力",
                example = "Children show remarkable resilience to change.",
                difficulty = 3,
                partOfSpeech = "n."
            ),
            Word(
                word = "stoic",
                phonetic = "/ˈstəʊɪk/",
                translation = "adj. 斯多葛派的，坚忍的",
                example = "He remained stoic despite the setbacks.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "tenacious",
                phonetic = "/təˈneɪʃəs/",
                translation = "adj. 顽强的，坚韧的",
                example = "She is a tenacious negotiator.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "unprecedented",
                phonetic = "/ʌnˈpresɪdentɪd/",
                translation = "adj. 前所未有的",
                example = "The crisis reached unprecedented proportions.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "versatile",
                phonetic = "/ˈvɜːsətaɪl/",
                translation = "adj. 多才多艺的，通用的",
                example = "She is a versatile athlete who excels in multiple sports.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            Word(
                word = "whimsical",
                phonetic = "/ˈwɪmzɪkl/",
                translation = "adj. 异想天开的，古怪的",
                example = "His whimsical sense of humor delights everyone.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "zealous",
                phonetic = "/ˈzeləs/",
                translation = "adj. 热心的，热情的",
                example = "She is a zealous advocate for animal rights.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "ambiguous",
                phonetic = "/æmˈbɪɡjuəs/",
                translation = "adj. 模棱两可的，含糊不清的",
                example = "The instructions were ambiguous and confusing.",
                difficulty = 4,
                partOfSpeech = "adj."
            ),
            Word(
                word = "brevity",
                phonetic = "/ˈbrevəti/",
                translation = "n. 简短，简洁",
                example = "The speaker was known for his brevity.",
                difficulty = 3,
                partOfSpeech = "n."
            ),
            Word(
                word = "coherent",
                phonetic = "/kəʊˈhɪərənt/",
                translation = "adj. 连贯的，一致的",
                example = "She presented a coherent argument.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            Word(
                word = "dichotomy",
                phonetic = "/daɪˈkɒtəmi/",
                translation = "n. 二分法，对立",
                example = "There is a dichotomy between theory and practice.",
                difficulty = 5,
                partOfSpeech = "n."
            ),
            Word(
                word = "eccentric",
                phonetic = "/ɪkˈsentrɪk/",
                translation = "adj. 古怪的，异乎寻常的",
                example = "The eccentric artist lived in a house made of bottles.",
                difficulty = 3,
                partOfSpeech = "adj."
            ),
            Word(
                word = "facade",
                phonetic = "/fəˈsɑːd/",
                translation = "n. 正面，表面，伪装",
                example = "Behind the facade of confidence, she was nervous.",
                difficulty = 4,
                partOfSpeech = "n."
            )
        )
    }
}
