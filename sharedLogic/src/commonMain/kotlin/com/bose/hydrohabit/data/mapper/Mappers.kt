package com.bose.hydrohabit.data.mapper

import com.bose.hydrohabit.domain.model.Achievement
import com.bose.hydrohabit.domain.model.AchievementId
import com.bose.hydrohabit.domain.model.AchievementType
import com.bose.hydrohabit.domain.model.ActivityLevel
import com.bose.hydrohabit.domain.model.DailyGoal
import com.bose.hydrohabit.domain.model.Gender
import com.bose.hydrohabit.domain.model.GoalSource
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.ReminderStrategy
import com.bose.hydrohabit.domain.model.Streak
import com.bose.hydrohabit.domain.model.TimeRange
import com.bose.hydrohabit.domain.model.UnitSystem
import com.bose.hydrohabit.domain.model.UserProfile
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.domain.model.EntrySource
import com.bose.hydrohabit.db.AchievementEntity
import com.bose.hydrohabit.db.DailyGoalEntity
import com.bose.hydrohabit.db.ReminderSettingsEntity
import com.bose.hydrohabit.db.StreakEntity
import com.bose.hydrohabit.db.UserProfileEntity
import com.bose.hydrohabit.db.WaterEntryEntity
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

// region UserProfile
fun UserProfileEntity.toDomain() = UserProfile(
    id = id,
    weightKg = weightKg,
    age = age.toInt(),
    gender = gender?.let { Gender.valueOf(it) },
    activityLevel = ActivityLevel.valueOf(activityLevel),
    wakeTime = LocalTime.parse(wakeTime),
    sleepTime = LocalTime.parse(sleepTime),
    unitSystem = UnitSystem.valueOf(unitSystem),
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
)
// endregion

// region WaterEntry
fun WaterEntryEntity.toDomain() = WaterEntry(
    id = id,
    amountMl = amountMl.toInt(),
    timestamp = Instant.fromEpochMilliseconds(timestamp),
    date = LocalDate.parse(date),
    source = EntrySource.valueOf(source),
    note = note,
)
// endregion

// region DailyGoal
fun DailyGoalEntity.toDomain() = DailyGoal(
    date = LocalDate.parse(date),
    targetMl = targetMl.toInt(),
    source = GoalSource.valueOf(source),
    baseMl = baseMl.toInt(),
    activityAdjustmentMl = activityAdjustmentMl.toInt(),
    weatherAdjustmentMl = weatherAdjustmentMl.toInt(),
)
// endregion

// region Streak
fun StreakEntity.toDomain() = Streak(
    currentDailyStreak = currentDailyStreak.toInt(),
    longestDailyStreak = longestDailyStreak.toInt(),
    currentWeeklyStreak = currentWeeklyStreak.toInt(),
    currentMonthlyStreak = currentMonthlyStreak.toInt(),
    lastQualifyingDate = lastQualifyingDate?.let { LocalDate.parse(it) },
)
// endregion

// region Achievement
fun AchievementEntity.toDomain() = Achievement(
    id = AchievementId.valueOf(id),
    title = title,
    description = description,
    type = AchievementType.valueOf(type),
    threshold = threshold,
    unlockedAt = unlockedAt?.let { Instant.fromEpochMilliseconds(it) },
    progress = progress.toFloat(),
)
// endregion

// region ReminderSettings
fun ReminderSettingsEntity.toDomain() = ReminderSettings(
    enabled = enabled != 0L,
    intervalMinutes = intervalMinutes.toInt(),
    wakeTime = LocalTime.parse(wakeTime),
    sleepTime = LocalTime.parse(sleepTime),
    strategy = ReminderStrategy.valueOf(strategy),
    quietWindows = decodeQuietWindows(quietWindowsJson),
    skipIfRecentlyLoggedMinutes = skipIfRecentlyLoggedMinutes.toInt(),
    soundEnabled = soundEnabled != 0L,
    vibrationEnabled = vibrationEnabled != 0L,
)

@Serializable
private data class TimeRangeDto(val start: String, val end: String)

fun encodeQuietWindows(windows: List<TimeRange>): String =
    json.encodeToString(windows.map { TimeRangeDto(it.start.toString(), it.end.toString()) })

fun decodeQuietWindows(jsonStr: String): List<TimeRange> {
    if (jsonStr.isBlank()) return emptyList()
    return json.decodeFromString<List<TimeRangeDto>>(jsonStr)
        .map { TimeRange(LocalTime.parse(it.start), LocalTime.parse(it.end)) }
}
// endregion
