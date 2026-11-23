package com.Demonad.habittracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HabitTest {
    private val date = LocalDate.of(2026, 11, 23)

    @Test
    fun targetsMatchTheSpec() {
        assertEquals(4, Targets.MEALS)
        assertEquals(1, Targets.FRIEND)
        assertEquals(1, Targets.SHAKE)
        assertEquals(7 * 60, Targets.SLEEP_MINUTES)
        assertEquals(5, Targets.GYM_DAYS_PER_WEEK)
        assertEquals(5, Targets.WORK_DAYS_PER_WEEK)
        assertEquals(3, Targets.REPORTS)
    }

    @Test
    fun mealsNeedFour() {
        assertFalse(DailyHabit.MEALS.isMet(DayLog(date, meals = 3)))
        assertTrue(DailyHabit.MEALS.isMet(DayLog(date, meals = 4)))
        assertTrue(DailyHabit.MEALS.isMet(DayLog(date, meals = 5)))
    }

    @Test
    fun friendAndShakeNeedOne() {
        assertFalse(DailyHabit.FRIEND.isMet(DayLog(date)))
        assertTrue(DailyHabit.FRIEND.isMet(DayLog(date, friend = 1)))
        assertFalse(DailyHabit.SHAKE.isMet(DayLog(date)))
        assertTrue(DailyHabit.SHAKE.isMet(DayLog(date, shake = 1)))
    }

    @Test
    fun sleepNeedsSevenHours() {
        assertFalse(DailyHabit.SLEEP.isMet(DayLog(date, sleepMinutes = 419)))
        assertTrue(DailyHabit.SLEEP.isMet(DayLog(date, sleepMinutes = 420)))
    }
}
