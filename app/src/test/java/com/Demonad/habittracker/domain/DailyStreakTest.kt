package com.Demonad.habittracker.domain

import com.Demonad.habittracker.domain.DayStatus.HIT
import com.Demonad.habittracker.domain.DayStatus.MISS
import com.Demonad.habittracker.domain.DayStatus.OFF
import com.Demonad.habittracker.domain.DayStatus.PENDING
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DailyStreakTest {
    private val today = LocalDate.of(2026, 11, 23)

    /** Statuses listed oldest first, ending today. */
    private fun run(vararg statuses: DayStatus): Streak {
        val first = today.minusDays(statuses.size - 1L)
        val map = statuses.withIndex().associate { (i, s) -> first.plusDays(i.toLong()) to s }
        return dailyStreak(map, today)
    }

    @Test
    fun emptyLogHasNoStreak() {
        assertEquals(Streak(0, 0), dailyStreak(emptyMap(), today))
    }

    @Test
    fun consecutiveHitsEndingTodayExtendTheStreak() {
        assertEquals(Streak(3, 3), run(HIT, HIT, HIT))
    }

    @Test
    fun pendingTodayDoesNotBreakTheStreak() {
        assertEquals(Streak(2, 2), run(HIT, HIT, PENDING))
    }

    @Test
    fun missingTodayEntryIsPendingNotAMiss() {
        val map = mapOf(today.minusDays(2) to HIT, today.minusDays(1) to HIT)
        assertEquals(Streak(2, 2), dailyStreak(map, today))
    }

    @Test
    fun missResetsCurrentToZeroAndKeepsBest() {
        assertEquals(Streak(0, 3), run(HIT, HIT, HIT, MISS))
    }

    @Test
    fun streakRestartsAfterAMiss() {
        assertEquals(Streak(1, 2), run(HIT, HIT, MISS, HIT))
    }

    @Test
    fun bestEverSurvivesALaterShorterRun() {
        assertEquals(Streak(2, 4), run(HIT, HIT, HIT, HIT, MISS, HIT, HIT))
    }

    @Test
    fun offDayNeitherExtendsNorBreaks() {
        assertEquals(Streak(2, 2), run(HIT, OFF, HIT))
    }

    @Test
    fun offDayAfterAMissDoesNotRevive() {
        assertEquals(Streak(0, 1), run(HIT, MISS, OFF))
    }

    @Test
    fun onlyOffDaysGiveNoStreak() {
        assertEquals(Streak(0, 0), run(OFF, OFF, OFF))
    }

    @Test
    fun pastGapsInTheLogCountAsMisses() {
        val map = mapOf(today.minusDays(3) to HIT, today to HIT)
        assertEquals(Streak(1, 1), dailyStreak(map, today))
    }

    @Test
    fun futureDatesAreIgnored() {
        val map = mapOf(today to HIT, today.plusDays(1) to HIT, today.plusDays(2) to HIT)
        assertEquals(Streak(1, 1), dailyStreak(map, today))
    }

    @Test
    fun singleHitToday() {
        assertEquals(Streak(1, 1), run(HIT))
    }
}
