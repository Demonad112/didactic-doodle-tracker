package com.Demonad.habittracker.domain

import com.Demonad.habittracker.domain.DayStatus.HIT
import com.Demonad.habittracker.domain.DayStatus.MISS
import com.Demonad.habittracker.domain.DayStatus.OFF
import com.Demonad.habittracker.domain.DayStatus.PENDING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PerfectWorkDayTest {
    private val today = LocalDate.of(2026, 11, 23)
    private val yesterday = today.minusDays(1)

    private fun perfect(date: LocalDate) =
        DayLog(
            date = date,
            reports = 3,
            policyViolations = 1,
            afterTheFactCharges = 1,
            otherItems = 1,
            storeEmailsDone = true,
        )

    @Test
    fun everyItemMetIsAPerfectDay() {
        assertTrue(isPerfectWorkDay(perfect(yesterday)))
    }

    @Test
    fun exceedingTheMinimumsStillCounts() {
        assertTrue(isPerfectWorkDay(perfect(yesterday).copy(reports = 7, otherItems = 4)))
    }

    @Test
    fun anySingleItemShortMakesItImperfect() {
        val base = perfect(yesterday)
        assertFalse(isPerfectWorkDay(base.copy(reports = 2)))
        assertFalse(isPerfectWorkDay(base.copy(policyViolations = 0)))
        assertFalse(isPerfectWorkDay(base.copy(afterTheFactCharges = 0)))
        assertFalse(isPerfectWorkDay(base.copy(otherItems = 0)))
        assertFalse(isPerfectWorkDay(base.copy(storeEmailsDone = false)))
    }

    @Test
    fun perfectPastDayIsAHit() {
        assertEquals(HIT, workDayStatus(perfect(yesterday), yesterday, today))
    }

    @Test
    fun imperfectPastDayIsAMiss() {
        assertEquals(MISS, workDayStatus(perfect(yesterday).copy(reports = 2), yesterday, today))
    }

    @Test
    fun pastDayWithNothingLoggedIsAMiss() {
        assertEquals(MISS, workDayStatus(null, yesterday, today))
    }

    @Test
    fun offDayIsAnAutomaticPassEvenWithNothingLogged() {
        assertEquals(OFF, workDayStatus(DayLog(yesterday, isOff = true), yesterday, today))
    }

    @Test
    fun offDayWinsOverMissingItems() {
        val log = perfect(yesterday).copy(reports = 0, isOff = true)
        assertEquals(OFF, workDayStatus(log, yesterday, today))
    }

    @Test
    fun unfinishedTodayIsPendingNotAMiss() {
        assertEquals(PENDING, workDayStatus(perfect(today).copy(reports = 1), today, today))
        assertEquals(PENDING, workDayStatus(null, today, today))
    }

    @Test
    fun finishedTodayIsAHit() {
        assertEquals(HIT, workDayStatus(perfect(today), today, today))
    }

    @Test
    fun offTodayIsOff() {
        assertEquals(OFF, workDayStatus(DayLog(today, isOff = true), today, today))
    }

    @Test
    fun offDayInTheMiddleDoesNotBreakTheWorkStreak() {
        val d1 = today.minusDays(2)
        val statuses =
            mapOf(
                d1 to workDayStatus(perfect(d1), d1, today),
                yesterday to workDayStatus(DayLog(yesterday, isOff = true), yesterday, today),
                today to workDayStatus(perfect(today), today, today),
            )
        assertEquals(Streak(2, 2), dailyStreak(statuses, today))
    }

    @Test
    fun resolveStatusCoversEveryCase() {
        assertEquals(HIT, resolveStatus(met = true, isOff = false, date = yesterday, today = today))
        assertEquals(MISS, resolveStatus(met = false, isOff = false, date = yesterday, today = today))
        assertEquals(PENDING, resolveStatus(met = false, isOff = false, date = today, today = today))
        assertEquals(OFF, resolveStatus(met = false, isOff = true, date = yesterday, today = today))
        assertEquals(OFF, resolveStatus(met = true, isOff = true, date = yesterday, today = today))
    }
}
