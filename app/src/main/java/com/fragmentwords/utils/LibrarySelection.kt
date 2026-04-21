package com.fragmentwords.utils

object LibrarySelection {

    const val CET4 = "CET4"
    const val CET6 = "CET6"
    const val IELTS = "IELTS"
    const val TOEFL = "TOEFL"
    const val GRE = "GRE"
    const val ADVANCED = "ADVANCED"

    val DEFAULT_SELECTED_LIBRARIES = listOf(CET4)

    private val supportedIds = linkedSetOf(
        CET4,
        CET6,
        IELTS,
        TOEFL,
        GRE,
        ADVANCED
    )

    fun normalize(libraries: Collection<String>): List<String> {
        val normalized = linkedSetOf<String>()
        libraries.forEach { rawId ->
            val normalizedId = normalizeSingle(rawId) ?: return@forEach
            normalized += normalizedId
        }
        return normalized.toList()
    }

    fun getDisplayName(selectedLibraries: List<String>): String {
        val normalized = normalize(selectedLibraries)
        return when {
            normalized.isEmpty() -> "四级词库"
            normalized.size > 1 -> "多个词库"
            normalized[0] == ADVANCED -> "高级词库"
            normalized[0] == CET4 -> "四级词库"
            normalized[0] == CET6 -> "六级词库"
            normalized[0] == IELTS -> "雅思词库"
            normalized[0] == TOEFL -> "托福词库"
            normalized[0] == GRE -> "GRE 词库"
            else -> normalized[0]
        }
    }

    fun toManagerId(selectionId: String): String {
        return (normalizeSingle(selectionId) ?: CET4).lowercase()
    }

    fun toDatabaseName(selectionId: String): String {
        return when (normalizeSingle(selectionId) ?: CET4) {
            ADVANCED -> "GRADUATE"
            else -> (normalizeSingle(selectionId) ?: CET4)
        }
    }

    fun fromManagerId(managerId: String): String? {
        return normalizeSingle(managerId)
    }

    private fun normalizeSingle(rawId: String?): String? {
        val normalized = rawId?.trim()?.uppercase().orEmpty()
        val canonical = when (normalized) {
            "GRAD" -> ADVANCED
            "GRADUATE" -> ADVANCED
            else -> normalized
        }
        return canonical.takeIf { it in supportedIds }
    }
}
