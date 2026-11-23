package com.Demonad.habittracker.domain

import java.time.LocalDate

/** Every work item met: 3 reports, 1 policy violation, 1 after-the-fact charge, 1 other, store 30/32 emails done. */
fun isPerfectWorkDay(log: DayLog): Boolean =
    log.reports >= Targets.REPORTS &&
        log.policyViolations >= Targets.POLICY_VIOLATIONS &&
        log.afterTheFactCharges >= Targets.AFTER_THE_FACT_CHARGES &&
        log.otherItems >= Targets.OTHER_ITEMS &&
        log.storeEmailsDone

/**
 * Status of one day for the combined perfect-work-day streak. A null [log] means nothing was recorded.
 * An off day is an automatic pass.
 */
fun workDayStatus(
    log: DayLog?,
    date: LocalDate,
    today: LocalDate,
): DayStatus =
    resolveStatus(
        met = log != null && isPerfectWorkDay(log),
        isOff = log?.isOff == true,
        date = date,
        today = today,
    )
