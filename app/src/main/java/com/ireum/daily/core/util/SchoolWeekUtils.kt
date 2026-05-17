package com.ireum.daily.core.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

fun LocalDate.startOfSchoolWeek(): LocalDate =
    when (dayOfWeek) {
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY -> with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        else -> with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
