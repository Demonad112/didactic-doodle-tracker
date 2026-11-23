package com.Demonad.habittracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class WeekTest {
    @Test
    fun sundayStartsItsOwnWeek() {
        val week = weekOf(LocalDate.of(2026, 11, 22))
        assertEquals(LocalDate.of(2026, 11, 22), week.start)
        assertEquals(LocalDate.of(2026, 11, 28), week.end)
    }

    @Test
    fun saturdayEndsItsWeek() {
        val week = weekOf(LocalDate.of(2026, 11, 28))
        assertEquals(LocalDate.of(2026, 11, 22), week.start)
        assertEquals(LocalDate.of(2026, 11, 28), week.end)
    }

    @Test
    fun midWeekDayMapsBackToSunday() {
        val week = weekOf(LocalDate.of(2026, 11, 23)) // Monday
        assertEquals(LocalDate.of(2026, 11, 22), week.start)
        assertEquals(DayOfWeek.SUNDAY, week.start.dayOfWeek)
        assertEquals(DayOfWeek.SATURDAY, week.end.dayOfWeek)
    }

    @Test
    fun weekSpansYearBoundary() {
        val week = weekOf(LocalDate.of(2026, 1, 1)) // Thursday
        assertEquals(LocalDate.of(2025, 12, 28), week.start)
        assertEquals(LocalDate.of(2026, 1, 3), week.end)
    }

    @Test
    fun weekHasSevenConsecutiveDates() {
        val dates = weekOf(LocalDate.of(2026, 11, 25)).dates()
        assertEquals(7, dates.size)
        assertEquals(LocalDate.of(2026, 11, 22), dates.first())
        assertEquals(LocalDate.of(2026, 11, 28), dates.last())
    }

    @Test
    fun containsIsInclusiveOfBothEnds() {
        val week = weekOf(LocalDate.of(2026, 11, 25))
        assertTrue(LocalDate.of(2026, 11, 22) in week)
        assertTrue(LocalDate.of(2026, 11, 28) in week)
        assertFalse(LocalDate.of(2026, 11, 21) in week)
        assertFalse(LocalDate.of(2026, 11, 29) in week)
    }
}
