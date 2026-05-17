package com.ireum.daily.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CoreUtilsTest {
    @Test
    fun startOfSchoolWeek_usesCurrentMondayOnWeekdays() {
        assertEquals(
            LocalDate.of(2026, 5, 18),
            LocalDate.of(2026, 5, 20).startOfSchoolWeek()
        )
    }

    @Test
    fun startOfSchoolWeek_usesNextMondayOnWeekends() {
        assertEquals(
            LocalDate.of(2026, 5, 25),
            LocalDate.of(2026, 5, 23).startOfSchoolWeek()
        )
        assertEquals(
            LocalDate.of(2026, 5, 25),
            LocalDate.of(2026, 5, 24).startOfSchoolWeek()
        )
    }

    @Test
    fun matchesFavoriteMenu_ignoresWhitespaceAndCase() {
        assertTrue("Spicy Fried Rice".matchesFavoriteMenu(listOf("friedrice")))
        assertTrue("Spicy Fried Rice".matchesFavoriteMenu(listOf("FRIED RICE")))
        assertFalse("Spicy Fried Rice".matchesFavoriteMenu(listOf("noodle")))
    }

    @Test
    fun findMatchingFavoriteMenus_returnsSortedMatches() {
        assertEquals(
            listOf("Apple", "Rice"),
            findMatchingFavoriteMenus(
                dishes = listOf("Rice Bowl", "Apple Juice"),
                favoriteMenus = listOf("Rice", "Apple", "Milk")
            )
        )
    }

    @Test
    fun subjectParticle_usesLastHangulSyllable() {
        assertEquals("이", "김밥".subjectParticle())
        assertEquals("가", "우유".subjectParticle())
        assertEquals("가", "돈까스, 우유".subjectParticle())
        assertEquals("가", "Pasta".subjectParticle())
    }

    @Test
    fun classifyTaskDueDate_groupsDatesRelativeToToday() {
        val today = LocalDate.of(2026, 5, 20)

        assertEquals(TaskDateCategory.OVERDUE, classifyTaskDueDate("2026-05-19", today))
        assertEquals(TaskDateCategory.TODAY, classifyTaskDueDate("2026-05-20", today))
        assertEquals(TaskDateCategory.TOMORROW, classifyTaskDueDate("2026-05-21", today))
        assertEquals(TaskDateCategory.THIS_WEEK, classifyTaskDueDate("2026-05-23", today))
        assertEquals(TaskDateCategory.FUTURE, classifyTaskDueDate("2026-05-25", today))
        assertEquals(TaskDateCategory.NO_DUE_DATE, classifyTaskDueDate(null, today))
        assertEquals(TaskDateCategory.NO_DUE_DATE, classifyTaskDueDate("bad-date", today))
    }
}
