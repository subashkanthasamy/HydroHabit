package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.domain.model.StreakConfig
import com.bose.hydrohabit.testutil.daySummary
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals

class StreakCalculatorTest {

    private val calculator = StreakCalculator()
    private val today = LocalDate(2026, 6, 18)
    private fun daysAgo(n: Int) = today.minus(n, DateTimeUnit.DAY)

    @Test
    fun emptyHistoryYieldsZeroStreak() {
        val streak = calculator.calculate(emptyList(), StreakConfig.DEFAULT, today)
        assertEquals(0, streak.currentDailyStreak)
        assertEquals(0, streak.longestDailyStreak)
        assertEquals(null, streak.lastQualifyingDate)
    }

    @Test
    fun consecutiveQualifyingDaysCountUpToToday() {
        val summaries = (0..3).map { daySummary(daysAgo(it), consumedMl = 2000, goalMl = 2000) }
        val streak = calculator.calculate(summaries, StreakConfig.DEFAULT, today)
        assertEquals(4, streak.currentDailyStreak)
        assertEquals(today, streak.lastQualifyingDate)
    }

    @Test
    fun todayInProgressDoesNotBreakStreak() {
        // Yesterday and before qualify; today logged but below goal (in progress).
        val summaries = listOf(
            daySummary(today, consumedMl = 500, goalMl = 2000),
            daySummary(daysAgo(1), consumedMl = 2000, goalMl = 2000),
            daySummary(daysAgo(2), consumedMl = 2000, goalMl = 2000),
        )
        val streak = calculator.calculate(summaries, StreakConfig.DEFAULT, today)
        assertEquals(2, streak.currentDailyStreak)
    }

    @Test
    fun gapBreaksCurrentStreak() {
        val summaries = listOf(
            daySummary(today, consumedMl = 2000, goalMl = 2000),
            // gap at daysAgo(1)
            daySummary(daysAgo(2), consumedMl = 2000, goalMl = 2000),
            daySummary(daysAgo(3), consumedMl = 2000, goalMl = 2000),
        )
        val streak = calculator.calculate(summaries, StreakConfig.DEFAULT, today)
        assertEquals(1, streak.currentDailyStreak)
        assertEquals(2, streak.longestDailyStreak)
    }

    @Test
    fun belowThresholdDayDoesNotQualify() {
        val summaries = (0..3).map { daySummary(daysAgo(it), consumedMl = 1000, goalMl = 2000) }
        val streak = calculator.calculate(summaries, StreakConfig.DEFAULT, today)
        assertEquals(0, streak.currentDailyStreak)
    }

    @Test
    fun weeklyStreakCountsQualifyingWeeks() {
        // 14 days, all qualifying ⇒ 2 full weeks meeting the 5-day threshold.
        val summaries = (0..13).map { daySummary(daysAgo(it), consumedMl = 2000, goalMl = 2000) }
        val streak = calculator.calculate(summaries, StreakConfig.DEFAULT, today)
        assertEquals(true, streak.currentWeeklyStreak >= 1)
    }
}
