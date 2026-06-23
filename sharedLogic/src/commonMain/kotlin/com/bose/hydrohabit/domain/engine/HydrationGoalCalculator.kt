package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.domain.model.DailyGoal
import com.bose.hydrohabit.domain.model.Gender
import com.bose.hydrohabit.domain.model.GoalSource
import com.bose.hydrohabit.domain.model.UserProfile
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

/**
 * Pure, deterministic hydration-goal calculation.
 *
 *   base   = weightKg * mlPerKg * ageFactor * genderFactor
 *   target = clamp( base * activityMultiplier + weatherAdjustment , min , max )
 *
 * The [weatherAdjustmentMl] parameter is the Phase 4 seam — callers pass 0 today.
 */
class HydrationGoalCalculator(
    private val config: GoalCalculatorConfig = GoalCalculatorConfig(),
) {
    fun calculate(
        profile: UserProfile,
        date: LocalDate,
        weatherAdjustmentMl: Int = 0,
    ): DailyGoal {
        val raw = profile.weightKg * config.mlPerKg *
            ageFactor(profile.age) * genderFactor(profile.gender)
        val baseMl = raw.roundToInt()

        val withActivity = (raw * profile.activityLevel.multiplier).roundToInt()
        val activityAdjustmentMl = withActivity - baseMl

        val target = (withActivity + weatherAdjustmentMl)
            .coerceIn(config.minMl, config.maxMl)

        return DailyGoal(
            date = date,
            targetMl = target,
            source = GoalSource.CALCULATED,
            baseMl = baseMl,
            activityAdjustmentMl = activityAdjustmentMl,
            weatherAdjustmentMl = weatherAdjustmentMl,
        )
    }

    private fun ageFactor(age: Int): Double = when {
        age <= 30 -> 1.0
        age <= 55 -> 0.97
        else -> 0.93
    }

    private fun genderFactor(gender: Gender?): Double = when (gender) {
        Gender.MALE -> 1.0
        Gender.FEMALE -> 0.95
        else -> 0.98
    }
}

/** Tunable constants. Centralized so they're easy to A/B or localize later. */
data class GoalCalculatorConfig(
    val mlPerKg: Double = 35.0,
    val minMl: Int = 1500,
    val maxMl: Int = 4000,
)
