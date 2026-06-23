package com.bose.hydrohabit.domain.usecase

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.time.atTime
import com.bose.hydrohabit.domain.engine.HydrationGoalCalculator
import com.bose.hydrohabit.domain.model.GoalSource
import com.bose.hydrohabit.testutil.FakeDailyGoalRepository
import com.bose.hydrohabit.testutil.FakeUserProfileRepository
import com.bose.hydrohabit.testutil.FixedTimeProvider
import com.bose.hydrohabit.testutil.sampleProfile
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoalUseCasesTest {

    private val date = LocalDate(2026, 6, 18)
    private val now = date.atTime(LocalTime(12, 0), TimeZone.UTC)
    private val profileRepo = FakeUserProfileRepository()
    private val goalRepo = FakeDailyGoalRepository()
    private val calculator = HydrationGoalCalculator()

    @Test
    fun saveProfileRecalculatesCalculatedGoal() = runTest {
        val save = SaveUserProfileUseCase(profileRepo, goalRepo, calculator, FixedTimeProvider(now))
        val result = save(sampleProfile(weightKg = 70.0))
        assertTrue(result is AppResult.Success)
        val goal = goalRepo.getGoal(date)
        assertEquals(2940, goal?.targetMl)
        assertEquals(GoalSource.CALCULATED, goal?.source)
    }

    @Test
    fun saveProfileDoesNotOverwriteManualGoal() = runTest {
        val setManual = SetManualGoalUseCase(goalRepo)
        setManual(date, 3500)

        val save = SaveUserProfileUseCase(profileRepo, goalRepo, calculator, FixedTimeProvider(now))
        save(sampleProfile(weightKg = 70.0))

        val goal = goalRepo.getGoal(date)
        assertEquals(3500, goal?.targetMl)
        assertEquals(GoalSource.MANUAL, goal?.source)
    }

    @Test
    fun saveProfileRejectsInvalidWeight() = runTest {
        val save = SaveUserProfileUseCase(profileRepo, goalRepo, calculator, FixedTimeProvider(now))
        val result = save(sampleProfile(weightKg = 5.0))
        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun calculateGoalRespectsManualOverride() = runTest {
        profileRepo.saveProfile(sampleProfile(weightKg = 70.0))
        SetManualGoalUseCase(goalRepo)(date, 3200)

        val calc = CalculateDailyGoalUseCase(profileRepo, goalRepo, calculator)
        val result = calc(date)
        assertTrue(result is AppResult.Success)
        assertEquals(3200, (result as AppResult.Success).value.targetMl)
    }
}
