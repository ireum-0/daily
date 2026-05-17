package com.ireum.daily.core.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class TaskDateCategory {
    OVERDUE,
    TODAY,
    TOMORROW,
    THIS_WEEK,
    FUTURE,
    NO_DUE_DATE
}

fun classifyTaskDueDate(
    dueDate: String?,
    today: LocalDate = LocalDate.now()
): TaskDateCategory {
    val parsedDueDate = dueDate
        ?.takeIf(String::isNotBlank)
        ?.let { value -> runCatching { LocalDate.parse(value) }.getOrNull() }
        ?: return TaskDateCategory.NO_DUE_DATE

    val tomorrow = today.plusDays(1)
    val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    return when {
        parsedDueDate.isBefore(today) -> TaskDateCategory.OVERDUE
        parsedDueDate == today -> TaskDateCategory.TODAY
        parsedDueDate == tomorrow -> TaskDateCategory.TOMORROW
        parsedDueDate <= endOfWeek -> TaskDateCategory.THIS_WEEK
        else -> TaskDateCategory.FUTURE
    }
}
