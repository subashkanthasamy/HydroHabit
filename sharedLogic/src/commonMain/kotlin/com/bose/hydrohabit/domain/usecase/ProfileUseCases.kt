package com.bose.hydrohabit.domain.usecase

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.DomainError
import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.core.util.IdGenerator
import com.bose.hydrohabit.domain.engine.HydrationGoalCalculator
import com.bose.hydrohabit.domain.model.ActivityLevel
import com.bose.hydrohabit.domain.model.Gender
import com.bose.hydrohabit.domain.model.GoalSource
import com.bose.hydrohabit.domain.model.UnitSystem
import com.bose.hydrohabit.domain.model.UserProfile
import com.bose.hydrohabit.domain.repository.DailyGoalRepository
import com.bose.hydrohabit.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime

/**
 * Validates and persists the profile, then recalculates today's goal unless the user has set a
 * manual override (a CALCULATED goal is always refreshed when the profile changes).
 */
class SaveUserProfileUseCase(
    private val profileRepository: UserProfileRepository,
    private val goalRepository: DailyGoalRepository,
    private val goalCalculator: HydrationGoalCalculator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(profile: UserProfile): AppResult<Unit> {
        validate(profile)?.let { return AppResult.Failure(it) }

        val now = timeProvider.now()
        val stamped = profile.copy(updatedAt = now)
        val saved = profileRepository.saveProfile(stamped)
        if (saved is AppResult.Failure) return saved

        val today = timeProvider.today()
        val existing = goalRepository.getGoal(today)
        if (existing?.source != GoalSource.MANUAL) {
            val goal = goalCalculator.calculate(stamped, today)
            return goalRepository.upsertGoal(goal)
        }
        return AppResult.Success(Unit)
    }

    private fun validate(profile: UserProfile): DomainError? = when {
        profile.weightKg !in 20.0..400.0 -> DomainError.Validation("Weight must be between 20 and 400 kg")
        profile.age !in 1..130 -> DomainError.Validation("Age must be between 1 and 130")
        else -> null
    }
}

class GetUserProfileUseCase(private val repository: UserProfileRepository) {
    operator fun invoke(): Flow<UserProfile?> = repository.observeProfile()
}

/**
 * Convenience for first-run setup: builds a [UserProfile] from primitive inputs (so UI layers
 * don't need kotlinx-datetime or to invent ids/timestamps) and saves it via [SaveUserProfileUseCase].
 */
class CreateInitialProfileUseCase(
    private val save: SaveUserProfileUseCase,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
) {
    suspend operator fun invoke(
        weightKg: Double,
        age: Int,
        gender: Gender? = null,
        activityLevel: ActivityLevel = ActivityLevel.MODERATE,
        wakeTime: LocalTime = LocalTime(7, 0),
        sleepTime: LocalTime = LocalTime(23, 0),
        unitSystem: UnitSystem = UnitSystem.METRIC,
    ): AppResult<Unit> {
        val now = timeProvider.now()
        return save(
            UserProfile(
                id = idGenerator.newId(),
                weightKg = weightKg,
                age = age,
                gender = gender,
                activityLevel = activityLevel,
                wakeTime = wakeTime,
                sleepTime = sleepTime,
                unitSystem = unitSystem,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}
