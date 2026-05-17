package com.ireum.daily.core.util

import com.ireum.daily.model.ImportWarning
import com.ireum.daily.model.ImportedTaskCandidate
import com.ireum.daily.model.TaskStatus
import java.time.LocalDate

object RiroSchoolTextParser {
    private val datePatterns = listOf(
        Regex("""(20\d{2})[-./](\d{1,2})[-./](\d{1,2})"""),
        Regex("""(\d{1,2})\s*월\s*(\d{1,2})\s*일""")
    )
    private val timePattern = Regex("""\d{1,2}\s*[:시]\s*\d{0,2}\s*분?""")
    private val explicitSubjectPattern = Regex("""^\s*[\[\(【]?([가-힣A-Za-z0-9 ]{1,12})[\]\)】]?\s*[:：]\s*(.+)$""")
    private val hyphenSubjectPattern = Regex("""^\s*([\[\(【]?[가-힣A-Za-z0-9 ]{1,12}[\]\)】]?)\s+-\s+(.+)$""")
    private val ignoredLineKeywords = listOf("과제", "수행평가", "마감", "제출", "상태", "전체")

    fun parse(text: String, today: LocalDate = LocalDate.now()): List<ImportedTaskCandidate> =
        text.toLogicalLines()
            .mapNotNull { line -> parseLine(line, today) }
            .take(MAX_CANDIDATES)

    private fun parseLine(line: String, today: LocalDate): ImportedTaskCandidate? {
        val dueDates = datePatterns.flatMap { pattern ->
            pattern.findAll(line).mapNotNull { match -> match.toDueDate(today) }
        }.distinct()
        val dueDate = dueDates.firstOrNull()
        val hasSpecificTime = timePattern.containsMatchIn(line)
        val subjectAndTitle = extractSubjectAndTitle(line)
        val title = cleanupTitle(subjectAndTitle.title)
        if (title.length < MIN_TITLE_LENGTH || title.all(Char::isDigit)) return null

        val warnings = buildList {
            if (dueDate == null) add(ImportWarning.MISSING_DUE_DATE)
            if (subjectAndTitle.subject.isNullOrBlank()) add(ImportWarning.MISSING_SUBJECT)
            if (dueDates.size > 1) add(ImportWarning.TOO_MANY_DATES)
            if (title.length < 5 || dueDate == null) add(ImportWarning.LOW_CONFIDENCE)
        }

        return ImportedTaskCandidate(
            title = title,
            subjectName = subjectAndTitle.subject,
            dueDate = dueDate,
            hasSpecificTime = hasSpecificTime,
            status = TaskStatus.TODO,
            confidence = if (warnings.contains(ImportWarning.LOW_CONFIDENCE)) 0.45f else 0.8f,
            rawText = line,
            warnings = warnings.distinct()
        )
    }

    private fun isIgnoredLine(line: String): Boolean {
        if (line.length < MIN_TITLE_LENGTH) return !line.looksLikeSubject()
        val normalized = line.filterNot(Char::isWhitespace)
        return ignoredLineKeywords.any { keyword -> normalized == keyword }
    }

    private fun String.toLogicalLines(): List<String> {
        val result = mutableListOf<String>()
        val pending = mutableListOf<String>()
        lines()
            .map(String::trim)
            .filter(String::isNotBlank)
            .filterNot(::isIgnoredLine)
            .forEach { line ->
                when {
                    line.containsDueDate() -> {
                        if (pending.isEmpty()) {
                            result += line
                        } else {
                            pending += line
                            result += pending.toLogicalLine()
                            pending.clear()
                        }
                    }

                    pending.isEmpty() -> pending += line

                    pending.size == 1 && pending.first().looksLikeSubjectOnlyLine() -> pending += line

                    else -> {
                        result += pending.toLogicalLine()
                        pending.clear()
                        pending += line
                    }
                }
            }

        if (pending.isNotEmpty()) {
            result += pending.toLogicalLine()
        }

        return result
    }

    private fun List<String>.toLogicalLine(): String =
        if (size >= 2 && first().looksLikeSubjectOnlyLine()) {
            "${first()}: ${drop(1).joinToString(" ")}"
        } else {
            joinToString(" ")
        }

    private fun extractSubjectAndTitle(line: String): SubjectAndTitle {
        val explicitMatch = explicitSubjectPattern.matchEntire(line)
        if (explicitMatch != null) {
            val subject = explicitMatch.groupValues[1].trimSubject().takeIf(String::isNotBlank)
            val title = explicitMatch.groupValues[2].trim()
            return SubjectAndTitle(subject = subject, title = title)
        }

        val hyphenMatch = hyphenSubjectPattern.matchEntire(line)
        if (hyphenMatch != null) {
            val subject = hyphenMatch.groupValues[1].trimSubject()
            val title = hyphenMatch.groupValues[2].trim()
            if (subject.looksLikeSubject() && cleanupTitle(title).isNotBlank()) {
                return SubjectAndTitle(subject = subject, title = title)
            }
        }

        return SubjectAndTitle(subject = null, title = line)
    }

    private fun cleanupTitle(line: String): String =
        datePatterns.fold(line) { current, pattern -> current.replace(pattern, " ") }
            .replace(timePattern, " ")
            .replace(Regex("""마감|기한|제출|까지|~"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim(' ', '-', ':', '：', '/', '|')

    private fun String.containsDueDate(): Boolean =
        datePatterns.any { pattern -> pattern.containsMatchIn(this) }

    private fun String.looksLikeSubjectOnlyLine(): Boolean =
        looksLikeSubject() && !containsDueDate() && !contains(':') && !contains('：') && !contains('-')

    private fun String.looksLikeSubject(): Boolean {
        val normalized = trimSubject()
        return normalized.length in 1..12 &&
            normalized.any { char -> char.isLetterOrDigit() } &&
            ignoredLineKeywords.none { keyword -> normalized == keyword }
    }

    private fun String.trimSubject(): String =
        trim()
            .trim('[', ']', '(', ')', '【', '】')
            .trim()

    private fun MatchResult.toDueDate(today: LocalDate): String? =
        runCatching {
            if (groupValues[1].length == 4) {
                LocalDate.of(
                    groupValues[1].toInt(),
                    groupValues[2].toInt(),
                    groupValues[3].toInt()
                )
            } else {
                val month = groupValues[1].toInt()
                val day = groupValues[2].toInt()
                val candidate = LocalDate.of(today.year, month, day)
                if (candidate.isBefore(today.minusMonths(6))) {
                    candidate.plusYears(1)
                } else {
                    candidate
                }
            }.toString()
        }.getOrNull()

    private data class SubjectAndTitle(
        val subject: String?,
        val title: String
    )

    private const val MIN_TITLE_LENGTH = 3
    private const val MAX_CANDIDATES = 20
}
