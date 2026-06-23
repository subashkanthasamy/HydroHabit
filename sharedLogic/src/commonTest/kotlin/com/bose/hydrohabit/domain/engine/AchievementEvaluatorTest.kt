package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.domain.model.AchievementId
import com.bose.hydrohabit.testutil.FixedTimeProvider
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AchievementEvaluatorTest {

    private val now = Instant.fromEpochMilliseconds(1_000_000)
    private val evaluator = AchievementEvaluator(FixedTimeProvider(now))

    private fun stats(
        totalEntries: Int = 0,
        totalVolumeMl: Long = 0,
        currentDailyStreak: Int = 0,
        longestDailyStreak: Int = 0,
        weeklyStreak: Int = 0,
        goalCompletionRate: Float = 0f,
    ) = LifetimeStats(totalEntries, totalVolumeMl, currentDailyStreak, longestDailyStreak, weeklyStreak, goalCompletionRate)

    @Test
    fun seedCatalogContainsAllMilestonesLocked() {
        val catalog = evaluator.seedCatalog()
        assertEquals(AchievementId.entries.size, catalog.size)
        assertTrue(catalog.all { !it.isUnlocked })
    }

    @Test
    fun firstLogUnlocksOnFirstEntry() {
        val result = evaluator.evaluate(evaluator.seedCatalog(), stats(totalEntries = 1))
        val firstLog = result.updated.first { it.id == AchievementId.FIRST_LOG }
        assertTrue(firstLog.isUnlocked)
        assertEquals(now, firstLog.unlockedAt)
        assertTrue(result.newlyUnlocked.any { it.id == AchievementId.FIRST_LOG })
    }

    @Test
    fun streak7ReportsPartialProgressBeforeUnlock() {
        val result = evaluator.evaluate(evaluator.seedCatalog(), stats(longestDailyStreak = 3))
        val streak7 = result.updated.first { it.id == AchievementId.STREAK_7 }
        assertFalse(streak7.isUnlocked)
        assertEquals(3f / 7f, streak7.progress)
    }

    @Test
    fun liters100UnlocksAt100Liters() {
        val result = evaluator.evaluate(evaluator.seedCatalog(), stats(totalVolumeMl = 100_000))
        assertTrue(result.updated.first { it.id == AchievementId.LITERS_100 }.isUnlocked)
    }

    @Test
    fun hydrationMasterRequiresAllSubGoals() {
        val partial = evaluator.evaluate(
            evaluator.seedCatalog(),
            stats(longestDailyStreak = 30, totalVolumeMl = 100_000, goalCompletionRate = 0.5f),
        )
        assertFalse(partial.updated.first { it.id == AchievementId.HYDRATION_MASTER }.isUnlocked)

        val full = evaluator.evaluate(
            evaluator.seedCatalog(),
            stats(longestDailyStreak = 30, totalVolumeMl = 100_000, goalCompletionRate = 0.95f),
        )
        assertTrue(full.updated.first { it.id == AchievementId.HYDRATION_MASTER }.isUnlocked)
    }

    @Test
    fun alreadyUnlockedAchievementsAreNotReReported() {
        val firstPass = evaluator.evaluate(evaluator.seedCatalog(), stats(totalEntries = 1))
        val secondPass = evaluator.evaluate(firstPass.updated, stats(totalEntries = 5))
        assertNotNull(secondPass.updated.first { it.id == AchievementId.FIRST_LOG }.unlockedAt)
        assertFalse(secondPass.newlyUnlocked.any { it.id == AchievementId.FIRST_LOG })
    }
}
