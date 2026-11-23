package com.Demonad.habittracker.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** A Sunday-to-Saturday week. Both ends are inclusive. */
data class WeekRange(val start: LocalDate, val end: LocalDate) {
    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    fun dates(): List<LocalDate> = generateSequence(start) { it.plusDays(1) }.takeWhile { it <= end }.toList()
}

fun weekOf(date: LocalDate): WeekRange {
    val start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    return WeekRange(start, start.plusDays(DAYS_IN_WEEK - 1L))
}

const val DAYS_IN_WEEK = 7
