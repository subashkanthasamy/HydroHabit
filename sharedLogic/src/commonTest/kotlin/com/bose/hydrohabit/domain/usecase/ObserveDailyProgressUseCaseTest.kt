package com.bose.hydrohabit.domain.usecase

import app.cash.turbine.test
import com.bose.hydrohabit.core.time.atTime
import com.bose.hydrohabit.domain.model.DailyGoal
import com.bose.hydrohabit.domain.model.EntrySource
import com.bose.hydrohabit.domain.model.GoalSource
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.testutil.FakeDailyGoalRepository
import com.bose.hydrohabit.testutil.FakeWaterEntryRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveDailyProgressUseCaseTest {

    private val date = LocalDate(2026, 6, 18)
    private val goalRepo = FakeDailyGoalRepository()
    private val entryRepo = FakeWaterEntryRepository()
    private val useCase = ObserveDailyProgressUseCase(goalRepo, entryRepo)

    @Test
    fun recomputesProgressAsGoalAndEntriesChange() = runTest {
        useCase(date).test {
            // Initial: no goal, no entries.
            with(awaitItem()) {
                assertEquals(0, goalMl)
                assertEquals(0, consumedMl)
                assertFalse(isCompleted)
            }

            goalRepo.state.value = mapOf(date to DailyGoal(date, 2000, GoalSource.CALCULATED, 2000, 0, 0))
            with(awaitItem()) {
                assertEquals(2000, goalMl)
                assertEquals(2000, remainingMl)
            }

            entryRepo.state.value = listOf(
                WaterEntry("a", 500, date.atTime(LocalTime(8, 0), TimeZone.UTC), date, EntrySource.QUICK_ADD),
            )
            with(awaitItem()) {
                assertEquals(500, consumedMl)
                assertEquals(1500, remainingMl)
                assertEquals(0.25f, completionPercent)
            }

            entryRepo.state.value = entryRepo.state.value + listOf(
                WaterEntry("b", 1500, date.atTime(LocalTime(9, 0), TimeZone.UTC), date, EntrySource.CUSTOM),
            )
            with(awaitItem()) {
                assertEquals(2000, consumedMl)
                assertTrue(isCompleted)
                assertEquals(1f, completionPercent)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
