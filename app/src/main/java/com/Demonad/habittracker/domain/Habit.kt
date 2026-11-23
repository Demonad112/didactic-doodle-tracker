package com.Demonad.habittracker.domain

/** Every target in the app, in one place. */
object Targets {
    const val MEALS = 4
    const val FRIEND = 1
    const val SHAKE = 1
    const val SLEEP_MINUTES = 7 * 60
    const val GYM_DAYS_PER_WEEK = 5
    const val WORK_DAYS_PER_WEEK = 5
    const val REPORTS = 3
    const val POLICY_VIOLATIONS = 1
    const val AFTER_THE_FACT_CHARGES = 1
    const val OTHER_ITEMS = 1
}

/** Personal habits with a daily target and their own streak. */
enum class DailyHabit {
    MEALS,
    FRIEND,
    SHAKE,
    SLEEP,
    ;

    fun isMet(log: DayLog): Boolean =
        when (this) {
            MEALS -> log.meals >= Targets.MEALS
            FRIEND -> log.friend >= Targets.FRIEND
            SHAKE -> log.shake >= Targets.SHAKE
            SLEEP -> log.sleepMinutes >= Targets.SLEEP_MINUTES
        }
}
