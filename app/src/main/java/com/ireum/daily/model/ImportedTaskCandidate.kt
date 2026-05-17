package com.ireum.daily.model

data class ImportedTaskCandidate(
    val title: String,
    val subjectName: String?,
    val dueDate: String?,
    val hasSpecificTime: Boolean,
    val status: TaskStatus,
    val confidence: Float,
    val rawText: String,
    val warnings: List<ImportWarning>
)

enum class ImportWarning {
    MISSING_DUE_DATE,
    MISSING_SUBJECT,
    LOW_CONFIDENCE,
    TOO_MANY_DATES
}
