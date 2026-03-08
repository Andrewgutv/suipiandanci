package com.fragmentwords.model

/**
 * 词库定义
 */
data class WordLibrary(
    val id: String,
    val name: String,
    val description: String,
    val icon: Int,
    val totalWords: Int
) {
    companion object {
        // 预定义词库
        val CET4 = WordLibrary("CET4", "四级词汇", "大学英语四级核心词汇", android.R.drawable.ic_menu_agenda, 4500)
        val CET6 = WordLibrary("CET6", "六级词汇", "大学英语六级核心词汇", android.R.drawable.ic_menu_edit, 2500)
        val IELTS = WordLibrary("IELTS", "雅思词汇", "雅思考试核心词汇", android.R.drawable.ic_menu_gallery, 3000)
        val TOEFL = WordLibrary("TOEFL", "托福词汇", "托福考试核心词汇", android.R.drawable.ic_menu_camera, 4000)
        val GRE = WordLibrary("GRE", "GRE词汇", "GRE考试核心词汇", android.R.drawable.ic_menu_call, 8000)
        val ADVANCED = WordLibrary("ADVANCED", "高级词汇", "日常高级英语词汇", android.R.drawable.ic_menu_view, 40)

        val ALL_LIBRARIES = listOf(CET4, CET6, IELTS, TOEFL, GRE, ADVANCED)
    }
}
