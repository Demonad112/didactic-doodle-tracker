package com.Demonad.habittracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Weeks used below (Sunday to Saturday):
 * W1 Nov 1-7, W2 Nov 8-14, W3 Nov 15-21, W4 Nov 22-28.
 */
class WeeklyStreakTest {
    private val w1 = LocalDate.of(2026, 11, 1)
    private val w2 = LocalDate.of(2026, 11, 8)
    private val w3 = LocalDate.of(2026, 11, 15)
    private val w4 = LocalDate.of(2026, 11, 22)

    /** Dates at the given day offsets from a week's Sunday. */
    private fun days(
        weekStart: LocalDate,
        vararg offsets: Int,
    ): Set<LocalDate> = offsets.map { weekStart.plusDays(it.toLong()) }.toSet()

    private fun result(
        qualifying: Set<LocalDate>,
        off: Set<LocalDate> = emptySet(),
        first: LocalDate = w1,
        today: LocalDate,
    ) = weeklyStreak(qualifying, off, target = 5, firstDate = first, today = today)

    @Test
    fun completedWeekAtTargetExtendsTheStreak() {
        val r = result(days(w1, 0, 1, 2, 3, 4), today = w2)
        assertEquals(Streak(1, 1), r.streak)
    }

    @Test
    fun completedWeekBelowTargetResetsToZero() {
        val r = result(days(w1, 0, 1, 2, 3, 4) + days(w2, 0, 1, 2, 3), today = w4)
        assertEquals(Streak(0, 1), r.streak)
    }

    @Test
    fun inProgressWeekNeverResetsAndShowsProgress() {
        val r = result(days(w1, 0, 1, 2, 3, 4) + days(w2, 0, 1), today = w2.plusDays(2))
        assertEquals(Streak(1, 1), r.streak)
        assertEquals(2, r.thisWeekCount)
        assertEquals(5, r.target)
    }

    @Test
    fun weekHitEarlyCountsOnceNotPerExtraDay() {
        val r = result(days(w4, 0, 1, 2, 3, 4, 5), first = w4, today = w4.plusDays(5))
        assertEquals(Streak(1, 1), r.streak)
        assertEquals(6, r.thisWeekCount)
    }

    @Test
    fun missResetsAndBestEverSurvives() {
        val r = result(days(w1, 0, 1, 2, 3, 4) + days(w2, 0, 1, 2, 3, 4) + days(w3, 0), today = w4)
        assertEquals(Streak(0, 2), r.streak)
    }

    @Test
    fun streakRestartsAfterAMissedWeek() {
        val q = days(w1, 0, 1, 2, 3, 4) + days(w3, 0, 1, 2, 3, 4)
        val r = result(q, today = w4)
        assertEquals(Streak(1, 1), r.streak)
    }

    @Test
    fun offDaysThatMakeTheTargetUnreachablePauseTheWeek() {
        val q = days(w1, 0, 1, 2, 3, 4) + days(w2, 0) + days(w3, 0, 1, 2, 3, 4)
        val off = days(w2, 1, 2, 3)
        val r = result(q, off, today = w4)
        assertEquals(Streak(2, 2), r.streak)
    }

    @Test
    fun offDaysThatLeaveTheTargetReachableStillMissWhenShort() {
        val q = days(w1, 0, 1, 2, 3, 4) + days(w2, 0, 1, 2, 3)
        val off = days(w2, 5, 6)
        val r = result(q, off, today = w4)
        assertEquals(Streak(0, 1), r.streak)
    }

    @Test
    fun twoOffDaysStillAllowAFiveDayWeek() {
        val r = result(days(w1, 0, 1, 2, 3, 4), off = days(w1, 5, 6), today = w2)
        assertEquals(Streak(1, 1), r.streak)
    }

    @Test
    fun partialFirstWeekIsNotPenalised() {
        val thursday = w1.plusDays(4)
        val r = result(days(w1, 4, 5) + days(w2, 0, 1, 2, 3, 4), first = thursday, today = w3)
        assertEquals(Streak(1, 1), r.streak)
    }

    @Test
    fun datesAfterTodayAreIgnored() {
        val r = result(days(w4, 0, 1, 2, 3, 4), first = w4, today = w4.plusDays(1))
        assertEquals(Streak(0, 0), r.streak)
        assertEquals(2, r.thisWeekCount)
    }

    @Test
    fun noDataGivesNoStreak() {
        val r = result(emptySet(), first = w4, today = w4)
        assertEquals(Streak(0, 0), r.streak)
        assertEquals(0, r.thisWeekCount)
    }
}
