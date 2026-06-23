package com.bose.hydrohabit.domain.model

import kotlinx.datetime.LocalDate

/**
 * Aggregated streak state. Recomputed by `StreakCalculator` from the day-by-day history; the
 * single persisted row is a cache of that computation.
 */
data class Streak(
    val currentDailyStreak: Int = 0,
    val longestDailyStreak: Int = 0,
    val currentWeeklyStreak: Int = 0,
    val currentMonthlyStreak: Int = 0,
    val lastQualifyingDate: LocalDate? = null,
) {
    companion object {
        val EMPTY = Streak()
    }
}

/**
 * Thresholds that decide what counts as success. Configurable per the requirements.
 *
 * @param dailySuccessPercent fraction of goal (0f..1f) a day must reach to qualify.
 * @param weeklyQualifyingDays qualifying days within a 7-day window for the week to count.
 * @param monthlyQualifyingDays qualifying days within a calendar month for the month to count.
 */
data class StreakConfig(
    val dailySuccessPercent: Float = 1.0f,
    val weeklyQualifyingDays: Int = 5,
    val monthlyQualifyingDays: Int = 20,
) {
    companion object {
        val DEFAULT = StreakConfig()
    }
}
