package com.bose.hydrohabit.domain.usecase

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.DomainError
import com.bose.hydrohabit.domain.engine.HydrationGoalCalculator
import com.bose.hydrohabit.domain.model.DailyGoal
import com.bose.hydrohabit.domain.model.GoalSource
import com.bose.hydrohabit.domain.repository.DailyGoalRepository
import com.bose.hydrohabit.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Recomputes a CALCULATED goal for [date]. Respects an existing MANUAL override. */
class CalculateDailyGoalUseCase(
    private val profileRepository: UserProfileRepository,
    private val goalRepository: DailyGoalRepository,
    private val goalCalculator: HydrationGoalCalculator,
) {
    suspend operator fun invoke(date: LocalDate, weatherAdjustmentMl: Int = 0): AppResult<DailyGoal> {
        val existing = goalRepository.getGoal(date)
        if (existing?.source == GoalSource.MANUAL) return AppResult.Success(existing)

        val profile = profileRepository.getProfile()
            ?: return AppResult.Failure(DomainError.Validation("Create a profile before calculating a goal"))

        val goal = goalCalculator.calculate(profile, date, weatherAdjustmentMl)
        return when (val r = goalRepository.upsertGoal(goal)) {
            is AppResult.Success -> AppResult.Success(goal)
            is AppResult.Failure -> r
        }
    }
}

/** Sets a user-defined goal that the engine must not overwrite. */
class SetManualGoalUseCase(private val goalRepository: DailyGoalRepository) {
    suspend operator fun invoke(date: LocalDate, targetMl: Int): AppResult<Unit> {
        if (targetMl <= 0) return AppResult.Failure(DomainError.Validation("Goal must be positive"))
        return goalRepository.upsertGoal(
            DailyGoal(
                date = date,
                targetMl = targetMl,
                source = GoalSource.MANUAL,
                baseMl = targetMl,
                activityAdjustmentMl = 0,
                weatherAdjustmentMl = 0,
            ),
        )
    }
}

class ObserveDailyGoalUseCase(private val goalRepository: DailyGoalRepository) {
    operator fun invoke(date: LocalDate): Flow<DailyGoal?> = goalRepository.observeGoal(date)
}
