package com.fragmentwords.model

import com.google.gson.annotations.SerializedName

/**
 * 单词数据模型
 */
data class Word(
    @SerializedName("word")
    val word: String,

    @SerializedName("phonetic")
    val phonetic: String,

    @SerializedName("translation")
    val translation: String,

    @SerializedName("example")
    val example: String,

    @SerializedName("difficulty")
    val difficulty: Int = 1,

    @SerializedName("partOfSpeech")
    val partOfSpeech: String = "",

    @SerializedName("library")
    val library: String = "" // 词库来源（CET4、CET6、IELTS等）
) {
    companion object {
        const val TABLE_NAME = "words"
        const val COLUMN_ID = "id"
        const val COLUMN_WORD = "word"
        const val COLUMN_PHONETIC = "phonetic"
        const val COLUMN_TRANSLATION = "translation"
        const val COLUMN_EXAMPLE = "example"
        const val COLUMN_DIFFICULTY = "difficulty"
        const val COLUMN_PART_OF_SPEECH = "part_of_speech"
        const val COLUMN_LIBRARY = "library"  // 新增词库字段
        const val COLUMN_TIMESTAMP = "timestamp"
    }
}
