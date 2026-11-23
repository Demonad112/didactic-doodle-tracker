# Spec: Habit Tracker — V1

## Objective

A native Android app for one user (Addy) that tracks personal and work habits with streaks, auto-fills sleep and gym from Health Connect, and keeps all data on the device.

**Why:** Accountability across competition prep and a store-ops job without hand-counting.

**Success looks like:** Open daily, log in under 30 seconds, trust the streak numbers.

### Habits

**Personal (daily, per-habit streak; each streak is independent)**

| Habit | Daily target | Source |
|---|---|---|
| Meals | 4 | Manual counter |
| Friend reached out to | 1 | Manual counter |
| Weight gainer shake | 1 | Manual counter |
| Sleep | ≥ 7h | Health Connect (overridable) |

**Gym (weekly)**: 5 of 7 days with a workout. Source: Health Connect exercise sessions (overridable). Streak counts consecutive weeks where the target was hit. Rest days never break anything.

**Work**

- **Weekly:** 5 of 7 days worked. Streak counts consecutive weeks where the target was hit.
- **Perfect work day (one combined daily streak, all items required):**
  - Reports: 3 minimum (counter)
  - Policy violation: 1 (counter)
  - After-the-fact charge: 1 (counter)
  - Other: 1 (counter)
  - Store 30/32 emails reviewed and followed up (checkbox)
- A day flagged off counts as an automatic pass for the perfect-work-day streak.
- A work day is any day not flagged off.

### Streak rules

- A miss hard-resets the streak to 0.
- Each streak shows its current and best-ever values.
- A manual **off/sick** flag on a day pauses affected streaks without breaking them. The day neither extends nor resets.
- Weeks run Sunday to Saturday.
- Sleep counts toward the day you wake up.
- Auto-filled sleep and gym values can be overridden manually.

### Data

- Local-only on the device. No cloud, no accounts, no analytics, no network calls.
- Health Connect is the only external data source, read-only, and only sleep and exercise sessions.

## Assumptions (correct me before planning)

1. Kotlin + Jetpack Compose, Room (SQLite) for storage, Health Connect client (`androidx.health.connect:connect-client`). Single module, no DI framework beyond constructor injection.
2. minSdk 28 (Health Connect client library floor). Target and compile SDK are the latest stable. Health Connect is either built in (Android 14+) or installed from Play on older versions.
3. Off/sick flag is per day and applies to all streaks (personal, gym, work) at once. A day flagged off is excluded from the work-days count for the week.
4. A week's gym target counts days with at least one exercise session of any type.
5. Sleep total for a day is the sum of sleep sessions ending on that date, in the device's time zone.
6. The "day" boundary is local midnight.
7. Weekly streak for a week in progress isn't judged until the week ends. The in-progress week shows progress (e.g. 3/5) and doesn't reset the streak early.
8. Backfilling past days is allowed (editing yesterday or earlier) because streaks are recomputed from the daily log, not stored as running totals.
9. Android Auto Backup is disabled for the app (`android:allowBackup="false"`) so "stays on this device" holds.
10. Weekly targets and off days: if off-flagged days leave fewer than 5 possible days in a week, that week is paused (neither extends nor resets) instead of missed. A first week that starts mid-week is never penalised.

## Tech Stack

- Kotlin, Jetpack Compose (Material 3), Room, Kotlin coroutines/Flow
- Health Connect client library
- JUnit + Turbine for unit tests, Room in-memory DB for DAO tests, Compose UI tests for key flows
- Gradle (Kotlin DSL), version catalog

## Commands

```
Build:        ./gradlew assembleDebug
Unit tests:   ./gradlew testDebugUnitTest
Instrumented: ./gradlew connectedDebugAndroidTest
Lint:         ./gradlew lintDebug ktlintCheck
Format:       ./gradlew ktlintFormat
```

## Project Structure

```
app/src/main/java/.../
  domain/     → Pure Kotlin: Habit definitions, DayLog, streak engine, week math
  data/       → Room entities/DAOs, repository, Health Connect reader
  ui/         → Compose screens (Today, Streaks, History), ViewModels
app/src/test/         → Unit tests (streak engine is the priority)
app/src/androidTest/  → DAO and UI tests
docs/                 → Intent and spec docs
```

The streak engine lives in `domain/` with no Android dependencies so it can be tested on the JVM.

## Code Style

Kotlin official style, enforced by ktlint. Pure functions in the domain layer. Streaks are derived from the log, never stored as mutable counters.

```kotlin
fun currentStreak(days: List<DayResult>, today: LocalDate): Int =
    days.sortedByDescending { it.date }
        .dropWhile { it.date == today && it.status == Status.PENDING }
        .takeWhile { it.status != Status.MISS }
        .count { it.status == Status.HIT }
```

Names: `PascalCase` types, `camelCase` functions, no abbreviations in domain names.

## Testing Strategy

- **Streak engine (JVM unit tests, highest priority):** hit/miss/off combinations, hard reset, best-ever, off-day pause, backfill recompute, Sunday–Saturday week boundaries, in-progress week, DST and time-zone day boundaries.
- **Data layer:** Room DAO tests with an in-memory DB. Health Connect reader tested against a fake interface.
- **UI:** Compose tests for logging a day, the off/sick toggle, and overriding an auto-filled value.
- **Manual:** On-device Health Connect permission flow and real data read.

## Boundaries

- **Always:** Test the streak engine before touching UI. Derive streaks from the log. Request only the Health Connect read permissions for sleep and exercise. Keep the app at zero network permissions.
- **Ask first:** Adding any dependency beyond those listed. Room schema changes after the first release. Any new Health Connect data type.
- **Never:** Add the `INTERNET` permission. Add analytics or crash reporting that phones home. Enable cloud backup. Store streak totals as mutable counters. Delete or skip a failing streak test.

## Success Criteria

1. All four personal habits, gym, and work items can be logged for a day in under 30 seconds from app open.
2. The streak engine passes its unit tests for every rule above, including hard reset, best-ever, off-day pause, and off-day auto-pass for work.
3. The weekly gym and work-day targets evaluate 5 of 7 on a Sunday–Saturday week, and rest days don't break the streak.
4. Sleep and gym populate from Health Connect after a permission grant, and a manual override persists and wins.
5. Editing a past day recomputes all affected streaks correctly.
6. The built APK declares no `INTERNET` permission and `allowBackup="false"`. Verified by inspecting the merged manifest.
7. The app works fully (manual entry) if Health Connect permission is denied or Health Connect is unavailable.

## Out of Scope (Deferred)

- Whoop/Noop integration: explicitly dropped.
- Cloud sync, backup, or export: conflicts with local-only. Possibly a manual file export in V2.
- iOS: native Android only.
- Reminders and notifications: not requested.
- Custom habit editing (add, remove, change targets in-app): V1 habits and targets are fixed in code.
- Charts and trends beyond streak and weekly progress.
- Home-screen widget.
- Multi-user.

## Open Questions

1. Is the stack in Assumption 1 (Kotlin/Compose/Room) fine, or do you want something else?
2. Should "worked" days be auto-inferred from logged work items, or always explicit via the off flag? (I assumed any non-off day is a work day, so a day with nothing logged on a work day is a miss.)
3. Should the Health Connect gym source accept any exercise type, or only specific ones such as strength training?
4. Do you need the off/sick flag to be separate for work vs. personal streaks, or one flag for everything? (I assumed one.)
