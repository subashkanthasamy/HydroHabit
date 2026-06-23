package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.domain.model.DaySummary
import com.bose.hydrohabit.domain.model.Streak
import com.bose.hydrohabit.domain.model.StreakConfig
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus

/**
 * Pure recomputation of daily / weekly / monthly streaks from the day-by-day history.
 *
 * "Today in progress" is handled: a non-qualifying current period doesn't break the streak — the
 * walk-back simply starts from the previous period. Gaps break it.
 */
class StreakCalculator {

    fun calculate(
        summaries: List<DaySummary>,
        config: StreakConfig,
        today: LocalDate,
    ): Streak {
        val qualifyingDays: Set<LocalDate> = summaries
            .filter { it.metGoal(config.dailySuccessPercent) }
            .map { it.date }
            .toSet()

        val currentDaily = walkBack(qualifyingDays, today) { it.minus(1, DateTimeUnit.DAY) }
        val longestDaily = longestDailyRun(qualifyingDays)

        // Weeks keyed by their Monday; a week qualifies when it has enough qualifying days.
        val weekCounts = qualifyingDays.groupingBy { weekStart(it) }.eachCount()
        val qualifyingWeeks = weekCounts.filterValues { it >= config.weeklyQualifyingDays }.keys
        val currentWeekly = walkBack(qualifyingWeeks, weekStart(today)) { it.minus(7, DateTimeUnit.DAY) }

        // Months keyed by their first day.
        val monthCounts = qualifyingDays.groupingBy { monthStart(it) }.eachCount()
        val qualifyingMonths = monthCounts.filterValues { it >= config.monthlyQualifyingDays }.keys
        val currentMonthly = walkBack(qualifyingMonths, monthStart(today)) { it.minus(1, DateTimeUnit.MONTH) }

        return Streak(
            currentDailyStreak = currentDaily,
            longestDailyStreak = maxOf(longestDaily, currentDaily),
            currentWeeklyStreak = currentWeekly,
            currentMonthlyStreak = currentMonthly,
            lastQualifyingDate = qualifyingDays.maxOrNull(),
        )
    }

    /**
     * Counts consecutive qualifying periods ending at [current]. If [current] itself isn't
     * qualifying yet (period in progress), counting begins from the previous period instead.
     */
    private fun walkBack(qualifying: Set<LocalDate>, current: LocalDate, previous: (LocalDate) -> LocalDate): Int {
        var key = if (current in qualifying) current else previous(current)
        var streak = 0
        while (key in qualifying) {
            streak++
            key = previous(key)
        }
        return streak
    }

    private fun longestDailyRun(days: Set<LocalDate>): Int {
        if (days.isEmpty()) return 0
        val sorted = days.sorted()
        var longest = 1
        var run = 1
        for (i in 1 until sorted.size) {
            run = if (sorted[i - 1].daysUntil(sorted[i]) == 1) run + 1 else 1
            if (run > longest) longest = run
        }
        return longest
    }

    private fun weekStart(date: LocalDate): LocalDate = date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    private fun monthStart(date: LocalDate): LocalDate = LocalDate(date.year, date.month, 1)
}
