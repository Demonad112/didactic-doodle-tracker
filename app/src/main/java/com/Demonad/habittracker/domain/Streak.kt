package com.Demonad.habittracker.domain

import java.time.LocalDate

data class Streak(val current: Int, val best: Int)

/**
 * Streak over consecutive days, derived from [statuses] on every call.
 *
 * The range runs from the earliest logged date through [today]. A past date with no entry is a miss,
 * and today with no entry is still pending. HIT extends, MISS resets to zero, OFF and PENDING change
 * nothing. Dates after [today] are ignored. The best-ever value survives resets.
 */
fun dailyStreak(
    statuses: Map<LocalDate, DayStatus>,
    today: LocalDate,
): Streak {
    val first = statuses.keys.filter { it <= today }.minOrNull() ?: return Streak(0, 0)
    var run = 0
    var best = 0
    var date = first
    while (date <= today) {
        val status = statuses[date] ?: if (date == today) DayStatus.PENDING else DayStatus.MISS
        when (status) {
            DayStatus.HIT -> {
                run++
                best = maxOf(best, run)
            }
            DayStatus.MISS -> run = 0
            DayStatus.OFF, DayStatus.PENDING -> Unit
        }
        date = date.plusDays(1)
    }
    return Streak(run, best)
}
