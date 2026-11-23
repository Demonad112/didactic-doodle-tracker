# Implementation Plan: Habit Tracker — V1

Source spec: `SPEC.md` (approved, defaults accepted)

## Overview

Native Android app (Kotlin, Compose, Room) with per-habit personal streaks, a combined perfect-work-day streak, weekly gym and work-day streaks, an off/sick flag, and Health Connect auto-fill for sleep and gym. All data stays on the device.

## Architecture Decisions

- **Streaks are derived, never stored.** A pure-Kotlin engine in `domain/` computes them from the daily log. Editing a past day then needs no special handling.
- **Domain layer has no Android imports.** The riskiest logic (streaks, week math) gets built and tested first, on the JVM.
- **Habits are fixed in code for V1.** There is no add/edit-habit UI. The user's "add a habit" request is deferred in the spec.
- **Storage is Room, not localStorage.** The spec is native Android.
- **Auto-filled values are stored beside manual overrides.** Resolution is `override ?: autoFilled ?: 0`. Overrides persist and always win.
- **Health Connect sits behind an interface.** It is tested against a fake. The real reader comes after the manual flow works, so the app is useful without it.
- **Vertical slices from Phase 2 on.** Each task ships something visible.
- **Package name:** `com.Demonad.habittracker` (confirmed by the user).

## Dependency Graph

```
T1 scaffold
 └─ T2 domain model + week math
     ├─ T3 daily streaks ──┐
     ├─ T4 weekly streaks ─┼─ T5 work perfect-day
     │                     │
     └─ T6 Room + repo ────┘
          ├─ T7 Today: personal ── T8 Today: work + off flag ── T9 Streaks screen
          │                                                     │
          ├─ T10 HC sleep ── T11 HC gym + permissions ── T12 override UI
          └─ T13 History + backfill (needs T9)
T14 manifest guard (needs T1) · T15 on-device pass (needs all)
```

## Task List

### Phase 1: Foundation and Streak Engine

- [x] **Task 1: Project scaffold and privacy guards**
- [x] **Task 2: Domain model and Sunday–Saturday week math**
- [x] **Task 3: Daily streak engine (hit / miss / off / pending)**
- [x] **Task 4: Weekly streak engine (gym 5/7, work days 5/7)**
- [x] **Task 5: Perfect-work-day evaluation with off auto-pass**

### Checkpoint: Engine
- [x] `./gradlew testDebugUnitTest` green, and every streak rule in SPEC.md has a test
- [ ] Review with human before persistence work

### Phase 2: Persistence and Manual Logging

- [ ] **Task 6: Room storage and repository for day logs**
- [ ] **Task 7: Today screen, personal habits**
- [ ] **Task 8: Today screen, work items and off/sick flag**
- [ ] **Task 9: Streaks screen (current, best-ever, weekly progress)**

### Checkpoint: Manual MVP
- [ ] Full manual flow works on a device or emulator: log, flag off, see streaks
- [ ] Logging a day takes under 30 seconds
- [ ] Review with human before Health Connect work

### Phase 3: Health Connect and History

- [ ] **Task 10: Health Connect sleep auto-fill**
- [ ] **Task 11: Health Connect gym auto-fill and permission flow**
- [ ] **Task 12: Override auto-filled values**
- [ ] **Task 13: History and past-day editing**

### Checkpoint: Feature Complete
- [ ] Sleep and gym populate from Health Connect after permission grant
- [ ] App is fully usable with permission denied
- [ ] Editing a past day recomputes streaks

### Phase 4: Hardening

- [ ] **Task 14: Manifest guard test (no INTERNET, no backup)**
- [ ] **Task 15: On-device verification pass**

### Checkpoint: Complete
- [ ] All 7 SPEC.md success criteria verified
- [ ] Ready for review

---

## Task Details

### Task 1: Project scaffold and privacy guards
**Description:** Gradle project with Compose, Room, Health Connect client, ktlint and a version catalog, with an empty Today screen that launches.
**Acceptance criteria:**
- [ ] `./gradlew assembleDebug` builds an APK that launches to a placeholder screen
- [ ] Manifest has no `INTERNET` permission and `allowBackup="false"`
- [ ] ktlint runs and passes
**Verification:** `./gradlew assembleDebug lintDebug ktlintCheck`
**Dependencies:** None
**Files:** `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml` (+ a stub `MainActivity`)
**Scope:** Medium. Build config is the exception to the 5-file guideline.

### Task 2: Domain model and Sunday–Saturday week math
**Description:** Pure-Kotlin types for the fixed habit set, `DayLog`, `DayStatus` (HIT/MISS/OFF/PENDING), and a `WeekRange` helper.
**Acceptance criteria:**
- [ ] Every habit and its daily target is defined in one place
- [ ] `weekOf(date)` returns Sunday–Saturday for any date, including year boundaries
- [ ] Domain package imports nothing from `android.*`
**Verification:** `./gradlew testDebugUnitTest --tests "*WeekTest"`
**Dependencies:** 1
**Files:** `domain/Habit.kt`, `domain/DayLog.kt`, `domain/Week.kt`, `WeekTest.kt`
**Scope:** Small

### Task 3: Daily streak engine
**Description:** Pure function that, given per-day results for one habit, returns current and best-ever streak.
**Acceptance criteria:**
- [ ] A miss resets the current streak to 0 and best-ever is preserved
- [ ] An OFF day neither extends nor breaks the streak
- [ ] Today still PENDING doesn't break the streak
- [ ] Gaps in the log (no row for a date) count as a miss for past days
**Verification:** `./gradlew testDebugUnitTest --tests "*DailyStreakTest"`. Write the tests first.
**Dependencies:** 2
**Files:** `domain/DailyStreak.kt`, `DailyStreakTest.kt`
**Scope:** Small

### Task 4: Weekly streak engine
**Description:** Streak over consecutive Sunday–Saturday weeks that hit a target of N days (gym 5, work 5).
**Acceptance criteria:**
- [ ] A completed week with ≥ 5 qualifying days extends the streak. With fewer, it resets to 0.
- [ ] The in-progress week shows progress (e.g. 3/5) and never resets the streak early
- [ ] A week can be hit before it ends, and still only counts once
- [ ] OFF days are excluded from the work-days count and never count as a miss
**Verification:** `./gradlew testDebugUnitTest --tests "*WeeklyStreakTest"`
**Dependencies:** 2
**Files:** `domain/WeeklyStreak.kt`, `WeeklyStreakTest.kt`
**Scope:** Small

### Task 5: Perfect-work-day evaluation
**Description:** Evaluates a day as HIT only when all work items are met, with OFF days as automatic passes, and feeds the daily streak engine.
**Acceptance criteria:**
- [ ] HIT requires reports ≥ 3, policy ≥ 1, after-the-fact ≥ 1, other ≥ 1 and the store 30/32 emails box checked
- [ ] Any single item short means MISS
- [ ] OFF day means pass (streak neither extended nor broken)
- [ ] A non-off day with nothing logged is a MISS once the day has passed
**Verification:** `./gradlew testDebugUnitTest --tests "*PerfectWorkDayTest"`
**Dependencies:** 3
**Files:** `domain/WorkDay.kt`, `PerfectWorkDayTest.kt`
**Scope:** Small

### Task 6: Room storage and repository
**Description:** `DayLogEntity` holds counters, the work checkbox, the off flag, and nullable auto-filled and override fields for sleep and gym. A repository exposes `Flow` and writes.
**Acceptance criteria:**
- [ ] A day log round-trips through the DB
- [ ] The repository exposes a date range as a `Flow`
- [ ] Value resolution is `override ?: autoFilled ?: 0` and is unit-tested
- [ ] Schema export is enabled
**Verification:** `./gradlew connectedDebugAndroidTest --tests "*DayLogDaoTest"`, using an in-memory DB
**Dependencies:** 2
**Files:** `data/DayLogEntity.kt`, `data/DayLogDao.kt`, `data/AppDatabase.kt`, `data/DayLogRepository.kt`, `DayLogDaoTest.kt`
**Scope:** Medium

### Task 7: Today screen, personal habits
**Description:** Vertical slice: counters for meals (4), friend (1) and shake (1) with sleep shown read-only for now, saved to Room, with a live streak next to each.
**Acceptance criteria:**
- [ ] Tapping +/− updates the count and persists across an app restart
- [ ] Each habit shows its target and its current streak
- [ ] Counts can't go below 0
**Verification:** UI test `TodayPersonalTest`, plus a manual check on an emulator
**Dependencies:** 3, 6
**Files:** `ui/TodayScreen.kt`, `ui/TodayViewModel.kt`, `ui/HabitRow.kt`, `MainActivity.kt`, `TodayPersonalTest.kt`
**Scope:** Medium

### Task 8: Today screen, work items and off/sick flag
**Description:** Work section with reports, policy, after-the-fact and other counters, the store 30/32 checkbox, and a single off/sick toggle for the day.
**Acceptance criteria:**
- [ ] Work items persist and show the perfect-work-day streak
- [ ] The off toggle hides or greys the work section, and the day counts as an auto-pass
- [ ] The off flag pauses all streaks for that day without breaking them
**Verification:** UI test `TodayWorkTest`, plus a manual check
**Dependencies:** 5, 7
**Files:** `ui/WorkSection.kt`, `ui/TodayScreen.kt`, `ui/TodayViewModel.kt`, `TodayWorkTest.kt`
**Scope:** Medium

### Task 9: Streaks screen
**Description:** Overview of each personal habit, the perfect-work-day streak, gym and work-day weekly streaks, each with current and best-ever values and weekly progress for the in-progress week.
**Acceptance criteria:**
- [ ] Every streak shows current and best-ever
- [ ] The weekly cards show progress (e.g. 3/5) for the current week
- [ ] Navigation between Today and Streaks works
**Verification:** UI test `StreaksScreenTest`, plus a manual check
**Dependencies:** 4, 8
**Files:** `ui/StreaksScreen.kt`, `ui/StreaksViewModel.kt`, `ui/AppNav.kt`, `StreaksScreenTest.kt`
**Scope:** Medium

### Task 10: Health Connect sleep auto-fill
**Description:** A `HealthDataSource` interface with a Health Connect implementation and a fake. Sleep sessions are summed per wake-up date in the device time zone and written into the day log's auto-filled field.
**Acceptance criteria:**
- [ ] A sleep session ending on date D counts toward D
- [ ] Multiple sessions on the same date are summed
- [ ] The time-zone and DST edge cases are unit-tested against the fake
- [ ] Only the sleep read permission is declared
**Verification:** `./gradlew testDebugUnitTest --tests "*SleepMappingTest"`
**Dependencies:** 6
**Files:** `data/HealthDataSource.kt`, `data/HealthConnectSource.kt`, `data/SleepMapping.kt`, `SleepMappingTest.kt`, `AndroidManifest.xml`
**Scope:** Medium

### Task 11: Health Connect gym auto-fill and permission flow
**Description:** Any exercise session on a date marks gym done for that date. Includes the permission request UI and the states for denied and unavailable.
**Acceptance criteria:**
- [ ] Any exercise type counts
- [ ] With permission denied or Health Connect missing, the app works fully with manual entry and shows a non-blocking notice
- [ ] Sync runs on app open and on pull-to-refresh
**Verification:** `./gradlew testDebugUnitTest --tests "*GymMappingTest"`, plus a manual permission-flow check on a device
**Dependencies:** 10
**Files:** `data/GymMapping.kt`, `data/SyncUseCase.kt`, `ui/PermissionPrompt.kt`, `ui/TodayScreen.kt`, `GymMappingTest.kt`
**Scope:** Medium

### Task 12: Override auto-filled values
**Description:** Sleep and gym rows show an "auto" badge and allow a manual override that persists and survives later syncs.
**Acceptance criteria:**
- [ ] Editing an auto-filled value stores an override and updates streaks
- [ ] A later sync doesn't overwrite an override
- [ ] An override can be cleared back to the auto value
**Verification:** UI test `OverrideTest`, plus a repository test for sync vs. override precedence
**Dependencies:** 11
**Files:** `ui/HabitRow.kt`, `ui/TodayViewModel.kt`, `data/DayLogRepository.kt`, `OverrideTest.kt`
**Scope:** Small

### Task 13: History and past-day editing
**Description:** A list or calendar of past days. Tapping one opens the same editor as Today, and all streaks recompute.
**Acceptance criteria:**
- [ ] Any past day can be viewed and edited
- [ ] Editing a past day changes the affected streaks immediately
- [ ] Future days can't be edited
**Verification:** UI test `HistoryEditTest`, plus a recompute unit test
**Dependencies:** 9
**Files:** `ui/HistoryScreen.kt`, `ui/DayEditor.kt`, `ui/AppNav.kt`, `HistoryEditTest.kt`
**Scope:** Medium

### Task 14: Manifest guard test
**Description:** A test or Gradle check that fails the build if the merged manifest contains `INTERNET` or enables backup.
**Acceptance criteria:**
- [ ] The check fails when `INTERNET` is added, verified once by adding it temporarily
- [ ] The check passes on the current manifest
**Verification:** `./gradlew testDebugUnitTest --tests "*ManifestGuardTest"`
**Dependencies:** 1
**Files:** `ManifestGuardTest.kt`, `app/build.gradle.kts`
**Scope:** Small

### Task 15: On-device verification pass
**Description:** Run through all 7 success criteria in SPEC.md on a real device with real Health Connect data. Fix or log anything found.
**Acceptance criteria:**
- [ ] Each SPEC.md success criterion is checked off with evidence
- [ ] Defects are fixed or logged as follow-ups
**Verification:** Manual run on a device, and a short results note in the PR
**Dependencies:** All
**Files:** None expected
**Scope:** Small

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Streak edge cases (OFF, gaps, week boundaries) are subtle | High | The engine is built and tested first (Tasks 3–5), before any UI |
| Health Connect availability and permissions vary by device and OS version | Med | An interface and fake, manual-entry fallback, and a real-device pass in Task 15 |
| Sleep and day attribution across time zones and DST | Med | Dedicated mapping tests in Task 10 |
| Gym auto-fill counts non-gym exercise (any type was chosen) | Low | Overridable. Revisit if it proves noisy |
| No Android device or emulator in this environment | Med | JVM unit tests cover the engine and mappings. UI and Health Connect checks need your device |
| Scope creep from deferred items (custom habits, reminders) | Med | Park them in the SPEC.md deferred list |

## Open Questions

1. ~~Package name~~ Resolved: `com.Demonad.habittracker`.
2. Can you run emulator or device checks on your machine, or should I limit verification to unit tests plus build and lint here?
