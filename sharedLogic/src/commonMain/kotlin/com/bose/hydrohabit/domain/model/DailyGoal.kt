package com.bose.hydrohabit.domain.model

import kotlinx.datetime.LocalDate

/**
 * The hydration target for a single day.
 *
 * Stored per-date so historical goals stay accurate even after the profile changes. The breakdown
 * fields ([baseMl], [activityAdjustmentMl], [weatherAdjustmentMl]) make the number explainable in
 * the UI and leave a seam for Phase 4 weather-based adjustments.
 */
data class DailyGoal(
    val date: LocalDate,
    val targetMl: Int,
    val source: GoalSource,
    val baseMl: Int,
    val activityAdjustmentMl: Int,
    val weatherAdjustmentMl: Int = 0,
)

/** Whether the goal was computed by the engine or set manually by the user. */
enum class GoalSource {
    /** Recomputed automatically when the profile changes. */
    CALCULATED,

    /** User override; the engine must not overwrite it. */
    MANUAL,
}
