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
}
