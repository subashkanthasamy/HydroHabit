package com.bose.hydrohabit.integration

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.time.atTime
import com.bose.hydrohabit.data.repository.AchievementRepositoryImpl
import com.bose.hydrohabit.data.repository.AnalyticsRepositoryImpl
import com.bose.hydrohabit.data.repository.DailyGoalRepositoryImpl
import com.bose.hydrohabit.data.repository.StreakRepositoryImpl
import com.bose.hydrohabit.data.repository.UserProfileRepositoryImpl
import com.bose.hydrohabit.data.repository.WaterEntryRepositoryImpl
import com.bose.hydrohabit.db.HydroHabitDatabase
import com.bose.hydrohabit.domain.engine.AchievementEvaluator
import com.bose.hydrohabit.domain.engine.AnalyticsCalculator
import com.bose.hydrohabit.domain.engine.HydrationGoalCalculator
import com.bose.hydrohabit.domain.engine.StreakCalculator
import com.bose.hydrohabit.domain.model.AchievementId
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.EntrySource
import com.bose.hydrohabit.domain.model.ReportPeriod
import com.bose.hydrohabit.domain.usecase.AddWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.EvaluateAchievementsUseCase
import com.bose.hydrohabit.domain.usecase.GenerateAnalyticsReportUseCase
import com.bose.hydrohabit.domain.usecase.ObserveDailyProgressUseCase
import com.bose.hydrohabit.domain.usecase.RecalculateStreakUseCase
import com.bose.hydrohabit.domain.usecase.SaveUserProfileUseCase
import com.bose.hydrohabit.testutil.FixedTimeProvider
import com.bose.hydrohabit.testutil.SequentialIdGenerator
import com.bose.hydrohabit.testutil.sampleProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * End-to-end test against the real SQLDelight schema (in-memory JDBC driver): the full happy path
 * profile -> goal -> entries -> progress -> streak/achievements -> analytics report.
 */
class DatabaseIntegrationTest {

    private val today = LocalDate(2026, 6, 18)
    private val now = today.atTime(LocalTime(12, 0), TimeZone.UTC)

    private fun newDatabase(): HydroHabitDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HydroHabitDatabase.Schema.create(driver)
        return HydroHabitDatabase(driver)
    }

    @Test
    fun fullHydrationFlowPersistsAndAggregatesCorrectly() = runTest {
        val db = newDatabase()
        val timeProvider = FixedTimeProvider(now)
        val ids = SequentialIdGenerator("entry")

        val profileRepo = UserProfileRepositoryImpl(db)
        val goalRepo = DailyGoalRepositoryImpl(db)
        val entryRepo = WaterEntryRepositoryImpl(db)
        val streakRepo = StreakRepositoryImpl(db)
        val achievementRepo = AchievementRepositoryImpl(db)
        val analyticsRepo = AnalyticsRepositoryImpl(db)

        // 1) Save profile → goal auto-calculates (70kg moderate male = 2940 ml).
        val save = SaveUserProfileUseCase(profileRepo, goalRepo, HydrationGoalCalculator(), timeProvider)
        assertTrue(save(sampleProfile(weightKg = 70.0)) is AppResult.Success)
        assertEquals(2940, goalRepo.getGoal(today)?.targetMl)

        // 2) Log entries totalling the goal.
        val addWater = AddWaterEntryUseCase(entryRepo, timeProvider, ids)
        addWater(1000, EntrySource.QUICK_ADD)
        addWater(1000, EntrySource.QUICK_ADD)
        addWater(940, EntrySource.CUSTOM)

        // 3) Progress is computed from persisted rows.
        val progress = ObserveDailyProgressUseCase(goalRepo, entryRepo)(today).first()
        assertEquals(2940, progress.consumedMl)
        assertEquals(3, progress.entryCount)
        assertTrue(progress.isCompleted)

        // 4) Streak: today met its goal → daily streak of 1.
        val streak = RecalculateStreakUseCase(analyticsRepo, streakRepo, StreakCalculator(), timeProvider)()
        assertEquals(1, streak.currentDailyStreak)
        assertEquals(today, streak.lastQualifyingDate)

        // 5) Achievements: first log unlocked, streak-7 partially progressed.
        val unlocked = EvaluateAchievementsUseCase(
            achievementRepo, entryRepo, streakRepo, analyticsRepo, AchievementEvaluator(timeProvider), timeProvider,
        )()
        assertTrue(unlocked.any { it.id == AchievementId.FIRST_LOG })
        val persisted = achievementRepo.getAchievements()
        assertEquals(1f / 7f, persisted.first { it.id == AchievementId.STREAK_7 }.progress)

        // 6) Analytics report aggregates the day correctly.
        val report = GenerateAnalyticsReportUseCase(analyticsRepo, AnalyticsCalculator(timeProvider, ids))(
            ReportPeriod.DAILY, DateRange(today, today),
        )
        assertEquals(2940L, report.totalMl)
        assertEquals(2940, report.averageMl)
        assertEquals(1f, report.goalCompletionRate)
        assertEquals(today, report.bestDay?.date)
    }
}
