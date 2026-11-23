package com.Demonad.habittracker.domain

import java.time.LocalDate

/**
 * One day of logged data with sleep and gym already resolved (override, else auto-filled, else zero).
 */
data class DayLog(
    val date: LocalDate,
    val meals: Int = 0,
    val friend: Int = 0,
    val shake: Int = 0,
    val sleepMinutes: Int = 0,
    val gymDone: Boolean = false,
    val reports: Int = 0,
    val policyViolations: Int = 0,
    val afterTheFactCharges: Int = 0,
    val otherItems: Int = 0,
    val storeEmailsDone: Boolean = false,
    val isOff: Boolean = false,
)

enum class DayStatus {
    /** Target met. Extends a streak. */
    HIT,

    /** Target missed on a day that has ended. Resets a streak. */
    MISS,

    /** Flagged off or sick. Neither extends nor breaks a streak. */
    OFF,

    /** Today, target not met yet. Neither extends nor breaks a streak. */
    PENDING,
}

/**
 * Turns "was the target met" into a status. Off wins over everything. A day that has ended without the
 * target is a miss, and today without it is still pending.
 */
fun resolveStatus(
    met: Boolean,
    isOff: Boolean,
    date: LocalDate,
    today: LocalDate,
): DayStatus =
    when {
        isOff -> DayStatus.OFF
        met -> DayStatus.HIT
        date < today -> DayStatus.MISS
        else -> DayStatus.PENDING
    }
