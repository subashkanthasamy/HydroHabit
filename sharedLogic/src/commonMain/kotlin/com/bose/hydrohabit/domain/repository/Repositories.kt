package com.bose.hydrohabit.domain.repository

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.domain.model.Achievement
import com.bose.hydrohabit.domain.model.AchievementId
import com.bose.hydrohabit.domain.model.DailyGoal
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.DaySummary
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.Streak
import com.bose.hydrohabit.domain.model.UserProfile
import com.bose.hydrohabit.domain.model.WaterEntry
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * Repository contracts. The domain layer owns these interfaces; the data layer implements them.
 *
 * Convention: reads are reactive [Flow]s sourced from the local DB (offline-first single source
 * of truth); writes are suspend functions returning [AppResult] so failures are explicit.
 */

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(profile: UserProfile): AppResult<Unit>
}

interface WaterEntryRepository {
    fun observeEntries(date: LocalDate): Flow<List<WaterEntry>>
    fun observeEntriesInRange(range: DateRange): Flow<List<WaterEntry>>
    suspend fun totalForDate(date: LocalDate): Int
    suspend fun addEntry(entry: WaterEntry): AppResult<Unit>
    suspend fun updateEntry(entry: WaterEntry): AppResult<Unit>
    suspend fun deleteEntry(id: String): AppResult<Unit>
    /** Lifetime aggregates for achievements. */
    suspend fun totalEntryCount(): Int
    suspend fun totalVolumeMl(): Long
}

interface DailyGoalRepository {
    fun observeGoal(date: LocalDate): Flow<DailyGoal?>
    suspend fun getGoal(date: LocalDate): DailyGoal?
    suspend fun upsertGoal(goal: DailyGoal): AppResult<Unit>
    suspend fun getGoalsInRange(range: DateRange): List<DailyGoal>
}

interface ReminderSettingsRepository {
    fun observeSettings(): Flow<ReminderSettings>
    suspend fun getSettings(): ReminderSettings
    suspend fun updateSettings(settings: ReminderSettings): AppResult<Unit>
}

interface StreakRepository {
    fun observeStreak(): Flow<Streak>
    suspend fun getStreak(): Streak
    suspend fun updateStreak(streak: Streak): AppResult<Unit>
}

interface AchievementRepository {
    fun observeAchievements(): Flow<List<Achievement>>
    suspend fun getAchievements(): List<Achievement>
    suspend fun upsertAll(achievements: List<Achievement>): AppResult<Unit>
    suspend fun unlock(id: AchievementId, at: Instant): AppResult<Unit>
}

interface AnalyticsRepository {
    /** Per-day rollups built from a single aggregate query over the range. */
    fun observeDailySummaries(range: DateRange): Flow<List<DaySummary>>
    suspend fun getDailySummaries(range: DateRange): List<DaySummary>
}
