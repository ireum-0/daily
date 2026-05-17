package com.ireum.daily.core.util

import com.ireum.daily.model.ImportWarning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RiroSchoolTextParserTest {
    @Test
    fun parse_extractsSubjectTitleAndFullDate() {
        val candidates = RiroSchoolTextParser.parse(
            text = "수학: 탐구 보고서 2026-05-21까지",
            today = LocalDate.of(2026, 5, 17)
        )

        assertEquals(1, candidates.size)
        assertEquals("수학", candidates.first().subjectName)
        assertEquals("탐구 보고서", candidates.first().title)
        assertEquals("2026-05-21", candidates.first().dueDate)
    }

    @Test
    fun parse_supportsKoreanMonthDay() {
        val candidates = RiroSchoolTextParser.parse(
            text = "영어 - 발표 준비 5월 23일",
            today = LocalDate.of(2026, 5, 17)
        )

        assertEquals(1, candidates.size)
        assertEquals("영어", candidates.first().subjectName)
        assertEquals("발표 준비", candidates.first().title)
        assertEquals("2026-05-23", candidates.first().dueDate)
    }

    @Test
    fun parse_keepsCandidateWithoutDateWithWarning() {
        val candidates = RiroSchoolTextParser.parse(
            text = "과학 실험 보고서 작성",
            today = LocalDate.of(2026, 5, 17)
        )

        assertEquals(1, candidates.size)
        assertEquals("과학 실험 보고서 작성", candidates.first().title)
        assertTrue(candidates.first().warnings.contains(ImportWarning.MISSING_DUE_DATE))
    }

    @Test
    fun parse_groupsSubjectTitleAndDateSplitAcrossLines() {
        val candidates = RiroSchoolTextParser.parse(
            text = """
                수학
                탐구 보고서
                2026-05-21
            """.trimIndent(),
            today = LocalDate.of(2026, 5, 17)
        )

        assertEquals(1, candidates.size)
        assertEquals("수학", candidates.first().subjectName)
        assertEquals("탐구 보고서", candidates.first().title)
        assertEquals("2026-05-21", candidates.first().dueDate)
    }

    @Test
    fun parse_doesNotTreatTitleHyphenDateAsSubjectOnly() {
        val candidates = RiroSchoolTextParser.parse(
            text = "탐구 보고서 - 2026-05-21까지",
            today = LocalDate.of(2026, 5, 17)
        )

        assertEquals(1, candidates.size)
        assertEquals(null, candidates.first().subjectName)
        assertEquals("탐구 보고서", candidates.first().title)
        assertEquals("2026-05-21", candidates.first().dueDate)
    }
}
