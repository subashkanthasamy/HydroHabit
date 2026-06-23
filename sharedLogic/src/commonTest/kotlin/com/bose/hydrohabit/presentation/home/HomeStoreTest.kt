package com.bose.hydrohabit.presentation.home

import com.bose.hydrohabit.core.time.atTime
import com.bose.hydrohabit.domain.engine.AchievementEvaluator
import com.bose.hydrohabit.domain.engine.ReminderScheduler
import com.bose.hydrohabit.domain.engine.StreakCalculator
import com.bose.hydrohabit.domain.model.AchievementId
import com.bose.hydrohabit.domain.model.DailyGoal
import com.bose.hydrohabit.domain.model.GoalSource
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.usecase.AddWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.ComputeReminderScheduleUseCase
import com.bose.hydrohabit.domain.usecase.EvaluateAchievementsUseCase
import com.bose.hydrohabit.domain.engine.AnalyticsCalculator
import com.bose.hydrohabit.domain.usecase.GetHydrationInsightsUseCase
import com.bose.hydrohabit.domain.usecase.GetQuickAddOptionsUseCase
import com.bose.hydrohabit.domain.usecase.GetUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.ObserveDailyProgressUseCase
import com.bose.hydrohabit.domain.usecase.ObserveEntriesForDateUseCase
import com.bose.hydrohabit.domain.usecase.ObserveStreakUseCase
import com.bose.hydrohabit.domain.usecase.QuickAddOption
import com.bose.hydrohabit.domain.usecase.RecalculateStreakUseCase
import com.bose.hydrohabit.domain.usecase.RescheduleRemindersUseCase
import com.bose.hydrohabit.testutil.FakeAchievementRepository
import com.bose.hydrohabit.testutil.FakeAnalyticsRepository
import com.bose.hydrohabit.testutil.FakeDailyGoalRepository
import com.bose.hydrohabit.testutil.FakeReminderSettingsRepository
import com.bose.hydrohabit.testutil.FakeStreakRepository
import com.bose.hydrohabit.testutil.FakeUserProfileRepository
import com.bose.hydrohabit.testutil.FakeWaterEntryRepository
import com.bose.hydrohabit.testutil.FixedTimeProvider
import com.bose.hydrohabit.testutil.RecordingPlatformReminderScheduler
import com.bose.hydrohabit.testutil.SequentialIdGenerator
import com.bose.hydrohabit.testutil.sampleProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStoreTest {

    private val today = LocalDate(2026, 6, 18)
    private val now = today.atTime(LocalTime(12, 0), TimeZone.UTC)

    @Test
    fun loadsStateThenQuickAddUpdatesProgressAndUnlocksFirstLog() = runTest {
        val timeProvider = FixedTimeProvider(now)
        val profileRepo = FakeUserProfileRepository()
        profileRepo.saveProfile(sampleProfile())
        val goalRepo = FakeDailyGoalRepository()
        goalRepo.upsertGoal(DailyGoal(today, 2000, GoalSource.CALCULATED, 2000, 0, 0))
        val entryRepo = FakeWaterEntryRepository()
        val streakRepo = FakeStreakRepository()
        val achievementRepo = FakeAchievementRepository()
        val analyticsRepo = FakeAnalyticsRepository()
        val settingsRepo = FakeReminderSettingsRepository(ReminderSettings.DEFAULT)
        val platformScheduler = RecordingPlatformReminderScheduler()
        val reschedule = RescheduleRemindersUseCase(
            ComputeReminderScheduleUseCase(
                settingsRepo, goalRepo, entryRepo, ReminderScheduler(SequentialIdGenerator("r")), timeProvider,
            ),
            settingsRepo,
            platformScheduler,
        )

        val store = HomeStore(
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            observeDailyProgress = ObserveDailyProgressUseCase(goalRepo, entryRepo),
            observeStreak = ObserveStreakUseCase(streakRepo),
            getUserProfile = GetUserProfileUseCase(profileRepo),
            getQuickAddOptions = GetQuickAddOptionsUseCase(),
            observeEntriesForDate = ObserveEntriesForDateUseCase(entryRepo),
            addWaterEntry = AddWaterEntryUseCase(entryRepo, timeProvider, SequentialIdGenerator("entry")),
            evaluateAchievements = EvaluateAchievementsUseCase(
                achievementRepo, entryRepo, streakRepo, analyticsRepo, AchievementEvaluator(timeProvider), timeProvider,
            ),
            recalculateStreak = RecalculateStreakUseCase(analyticsRepo, streakRepo, StreakCalculator(), timeProvider),
            rescheduleReminders = reschedule,
            getHydrationInsights = GetHydrationInsightsUseCase(analyticsRepo, AnalyticsCalculator(timeProvider, SequentialIdGenerator("ins"))),
            timeProvider = timeProvider,
        )

        // Initial load resolved synchronously under the unconfined dispatcher.
        with(store.state.value) {
            assertEquals(today, date)
            assertFalse(isLoading)
            assertEquals(2000, progress?.goalMl)
            assertEquals(0, progress?.consumedMl)
            assertEquals(4, quickAddOptions.size)
        }

        store.dispatch(HomeIntent.AddQuickAdd(QuickAddOption(250, "250ml")))

        with(store.state.value) {
            assertEquals(250, progress?.consumedMl)
            assertTrue(newlyUnlocked.any { it.id == AchievementId.FIRST_LOG })
        }
        // Logging re-plans reminders (goal not yet met → at least one scheduled).
        assertTrue(platformScheduler.lastScheduled.isNotEmpty())
    }
}
