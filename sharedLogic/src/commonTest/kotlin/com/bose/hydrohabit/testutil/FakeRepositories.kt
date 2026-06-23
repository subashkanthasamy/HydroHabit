package com.bose.hydrohabit.testutil

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.domain.model.Achievement
import com.bose.hydrohabit.domain.model.AchievementId
import com.bose.hydrohabit.domain.model.DailyGoal
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.DaySummary
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.ScheduledReminder
import com.bose.hydrohabit.domain.model.Streak
import com.bose.hydrohabit.domain.model.UserProfile
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.domain.reminders.PlatformReminderScheduler
import com.bose.hydrohabit.domain.repository.AchievementRepository
import com.bose.hydrohabit.domain.repository.AnalyticsRepository
import com.bose.hydrohabit.domain.repository.DailyGoalRepository
import com.bose.hydrohabit.domain.repository.ReminderSettingsRepository
import com.bose.hydrohabit.domain.repository.StreakRepository
import com.bose.hydrohabit.domain.repository.UserProfileRepository
import com.bose.hydrohabit.domain.repository.WaterEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class FakeUserProfileRepository : UserProfileRepository {
    private val state = MutableStateFlow<UserProfile?>(null)
    override fun observeProfile(): Flow<UserProfile?> = state
    override suspend fun getProfile(): UserProfile? = state.value
    override suspend fun saveProfile(profile: UserProfile): AppResult<Unit> {
        state.value = profile
        return AppResult.Success(Unit)
    }
}

class FakeWaterEntryRepository : WaterEntryRepository {
    val state = MutableStateFlow<List<WaterEntry>>(emptyList())
    override fun observeEntries(date: LocalDate): Flow<List<WaterEntry>> =
        state.map { list -> list.filter { it.date == date } }
    override fun observeEntriesInRange(range: DateRange): Flow<List<WaterEntry>> =
        state.map { list -> list.filter { it.date >= range.start && it.date <= range.end } }
    override suspend fun totalForDate(date: LocalDate): Int =
        state.value.filter { it.date == date }.sumOf { it.amountMl }
    override suspend fun addEntry(entry: WaterEntry): AppResult<Unit> {
        state.value = state.value + entry
        return AppResult.Success(Unit)
    }
    override suspend fun updateEntry(entry: WaterEntry): AppResult<Unit> {
        state.value = state.value.map { if (it.id == entry.id) entry else it }
        return AppResult.Success(Unit)
    }
    override suspend fun deleteEntry(id: String): AppResult<Unit> {
        state.value = state.value.filterNot { it.id == id }
        return AppResult.Success(Unit)
    }
    override suspend fun totalEntryCount(): Int = state.value.size
    override suspend fun totalVolumeMl(): Long = state.value.sumOf { it.amountMl.toLong() }
}

class FakeDailyGoalRepository : DailyGoalRepository {
    val state = MutableStateFlow<Map<LocalDate, DailyGoal>>(emptyMap())
    override fun observeGoal(date: LocalDate): Flow<DailyGoal?> = state.map { it[date] }
    override suspend fun getGoal(date: LocalDate): DailyGoal? = state.value[date]
    override suspend fun upsertGoal(goal: DailyGoal): AppResult<Unit> {
        state.value = state.value + (goal.date to goal)
        return AppResult.Success(Unit)
    }
    override suspend fun getGoalsInRange(range: DateRange): List<DailyGoal> =
        state.value.values.filter { it.date >= range.start && it.date <= range.end }
}

class FakeReminderSettingsRepository(initial: ReminderSettings) : ReminderSettingsRepository {
    val state = MutableStateFlow(initial)
    override fun observeSettings(): Flow<ReminderSettings> = state
    override suspend fun getSettings(): ReminderSettings = state.value
    override suspend fun updateSettings(settings: ReminderSettings): AppResult<Unit> {
        state.value = settings
        return AppResult.Success(Unit)
    }
}

class FakeStreakRepository : StreakRepository {
    val state = MutableStateFlow(Streak.EMPTY)
    override fun observeStreak(): Flow<Streak> = state
    override suspend fun getStreak(): Streak = state.value
    override suspend fun updateStreak(streak: Streak): AppResult<Unit> {
        state.value = streak
        return AppResult.Success(Unit)
    }
}

class FakeAchievementRepository : AchievementRepository {
    val state = MutableStateFlow<List<Achievement>>(emptyList())
    override fun observeAchievements(): Flow<List<Achievement>> = state
    override suspend fun getAchievements(): List<Achievement> = state.value
    override suspend fun upsertAll(achievements: List<Achievement>): AppResult<Unit> {
        state.value = achievements
        return AppResult.Success(Unit)
    }
    override suspend fun unlock(id: AchievementId, at: Instant): AppResult<Unit> {
        state.value = state.value.map { if (it.id == id) it.copy(unlockedAt = at) else it }
        return AppResult.Success(Unit)
    }
}

class FakeAnalyticsRepository(private val summaries: List<DaySummary> = emptyList()) : AnalyticsRepository {
    val state = MutableStateFlow(summaries)
    override fun observeDailySummaries(range: DateRange): Flow<List<DaySummary>> =
        state.map { list -> list.filter { it.date >= range.start && it.date <= range.end } }
    override suspend fun getDailySummaries(range: DateRange): List<DaySummary> =
        state.value.filter { it.date >= range.start && it.date <= range.end }
}

class RecordingPlatformReminderScheduler : PlatformReminderScheduler {
    var lastScheduled: List<ScheduledReminder> = emptyList()
    var cancelled = false
    override suspend fun schedule(reminders: List<ScheduledReminder>) { lastScheduled = reminders }
    override suspend fun cancelAll() { cancelled = true }
}
