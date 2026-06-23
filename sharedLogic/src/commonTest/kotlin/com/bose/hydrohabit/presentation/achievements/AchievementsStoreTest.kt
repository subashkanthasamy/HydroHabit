package com.bose.hydrohabit.presentation.achievements

import com.bose.hydrohabit.domain.engine.AchievementEvaluator
import com.bose.hydrohabit.domain.model.AchievementId
import com.bose.hydrohabit.domain.usecase.ObserveAchievementsUseCase
import com.bose.hydrohabit.testutil.FakeAchievementRepository
import com.bose.hydrohabit.testutil.FixedTimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsStoreTest {

    private val evaluator = AchievementEvaluator(FixedTimeProvider(Instant.fromEpochMilliseconds(0)))

    @Test
    fun fallsBackToSeedCatalogWhenEmpty() = runTest {
        val repo = FakeAchievementRepository()
        val store = AchievementsStore(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            ObserveAchievementsUseCase(repo),
            evaluator,
        )
        assertEquals(AchievementId.entries.size, store.state.value.achievements.size)
        assertFalse(store.state.value.achievements.any { it.isUnlocked })
        assertEquals(0, store.state.value.unlockedCount)
    }

    @Test
    fun reflectsPersistedAchievements() = runTest {
        val repo = FakeAchievementRepository()
        val store = AchievementsStore(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            ObserveAchievementsUseCase(repo),
            evaluator,
        )
        val unlocked = evaluator.seedCatalog().map {
            if (it.id == AchievementId.FIRST_LOG) it.copy(unlockedAt = Instant.fromEpochMilliseconds(1)) else it
        }
        repo.upsertAll(unlocked)

        assertTrue(store.state.value.achievements.first { it.id == AchievementId.FIRST_LOG }.isUnlocked)
        assertEquals(1, store.state.value.unlockedCount)
    }
}
