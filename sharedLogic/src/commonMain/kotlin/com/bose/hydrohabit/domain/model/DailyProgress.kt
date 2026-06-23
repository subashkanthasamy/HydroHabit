package com.bose.hydrohabit.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Derived hydration status for a day. Never persisted — computed on demand from the day's goal
 * and entries so it is always consistent with the source of truth.
 */
data class DailyProgress(
    val date: LocalDate,
    val goalMl: Int,
    val consumedMl: Int,
    val entryCount: Int,
    val lastEntryAt: Instant?,
) {
    val remainingMl: Int get() = (goalMl - consumedMl).coerceAtLeast(0)

    /** 0f..1f; capped so over-drinking doesn't report >100%. */
    val completionPercent: Float
        get() = if (goalMl <= 0) 0f else (consumedMl.toFloat() / goalMl).coerceIn(0f, 1f)

    val isCompleted: Boolean get() = goalMl > 0 && consumedMl >= goalMl

    companion object {
        fun empty(date: LocalDate, goalMl: Int) =
            DailyProgress(date = date, goalMl = goalMl, consumedMl = 0, entryCount = 0, lastEntryAt = null)
    }
}
