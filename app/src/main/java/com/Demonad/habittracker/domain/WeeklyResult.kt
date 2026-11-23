package com.Demonad.habittracker.domain

import java.time.LocalDate

data class WeeklyResult(
    val streak: Streak,
    /** Qualifying days so far in the week that contains today. */
    val thisWeekCount: Int,
    val target: Int,
)

/**
 * Streak over consecutive Sunday-to-Saturday weeks that reached [target] qualifying days.
 *
 * - A week at or above target is a hit the moment it gets there, and counts once.
 * - If [off] days leave fewer than [target] possible days, the week is paused instead of missed.
 * - The week in progress never resets the streak.
 * - A first week that starts after its Sunday is never penalised.
 * - Dates after [today] are ignored.
 *
 * The caller decides what qualifies: gym passes days with a workout, and work passes every non-off day.
 */
fun weeklyStreak(
    qualifying: Set<LocalDate>,
    off: Set<LocalDate>,
    target: Int,
    firstDate: LocalDate,
    today: LocalDate,
): WeeklyResult {
    val currentWeek = weekOf(today)
    var run = 0
    var best = 0
    var week = weekOf(firstDate)
    while (week.start <= currentWeek.start) {
        val count = qualifying.count { it in week && it <= today }
        val offCount = off.count { it in week && it <= today }
        when {
            count >= target -> {
                run++
                best = maxOf(best, run)
            }
            week.start < firstDate -> Unit
            DAYS_IN_WEEK - offCount < target -> Unit
            week.start == currentWeek.start -> Unit
            else -> run = 0
        }
        week = weekOf(week.end.plusDays(1))
    }
    val thisWeekCount = qualifying.count { it in currentWeek && it <= today }
    return WeeklyResult(Streak(run, best), thisWeekCount, target)
}
