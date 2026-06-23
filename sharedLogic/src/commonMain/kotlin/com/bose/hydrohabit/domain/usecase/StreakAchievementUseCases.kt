package com.bose.hydrohabit.domain.usecase

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.engine.AchievementEvaluator
import com.bose.hydrohabit.domain.engine.LifetimeStats
import com.bose.hydrohabit.domain.engine.StreakCalculator
import com.bose.hydrohabit.domain.model.Achievement
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.Streak
import com.bose.hydrohabit.domain.model.StreakConfig
import com.bose.hydrohabit.domain.repository.AchievementRepository
import com.bose.hydrohabit.domain.repository.AnalyticsRepository
import com.bose.hydrohabit.domain.repository.StreakRepository
import com.bose.hydrohabit.domain.repository.WaterEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

class ObserveStreakUseCase(private val repository: StreakRepository) {
    operator fun invoke(): Flow<Streak> = repository.observeStreak()
}

/** Recomputes streaks from the recent history window and persists the result. */
class RecalculateStreakUseCase(
    private val analyticsRepository: AnalyticsRepository,
    private val streakRepository: StreakRepository,
    private val calculator: StreakCalculator,
    private val timeProvider: TimeProvider,
    private val config: StreakConfig = StreakConfig.DEFAULT,
) {
    suspend operator fun invoke(): Streak {
        val today = timeProvider.today()
        val range = DateRange(today.minus(LOOKBACK_DAYS, DateTimeUnit.DAY), today)
        val summaries = analyticsRepository.getDailySummaries(range)
        val streak = calculator.calculate(summaries, config, today)
        streakRepository.updateStreak(streak)
        return streak
    }

    companion object { const val LOOKBACK_DAYS = 400 }
}

class ObserveAchievementsUseCase(private val repository: AchievementRepository) {
    operator fun invoke(): Flow<List<Achievement>> = repository.observeAchievements()
}

/**
 * Recomputes achievement state from lifetime stats and persists it. Returns the achievements newly
 * unlocked by this evaluation so the UI can celebrate them. Run after each successful log.
 */
class EvaluateAchievementsUseCase(
    private val achievementRepository: AchievementRepository,
    private val entryRepository: WaterEntryRepository,
    private val streakRepository: StreakRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val evaluator: AchievementEvaluator,
    private val timeProvider: TimeProvider,
    private val streakConfig: StreakConfig = StreakConfig.DEFAULT,
) {
    suspend operator fun invoke(): List<Achievement> {
        val current = achievementRepository.getAchievements().ifEmpty { evaluator.seedCatalog() }
        val streak = streakRepository.getStreak()

        val today = timeProvider.today()
        val range = DateRange(today.minus(RecalculateStreakUseCase.LOOKBACK_DAYS, DateTimeUnit.DAY), today)
        val summaries = analyticsRepository.getDailySummaries(range)
        val daysWithGoal = summaries.filter { it.goalMl > 0 }
        val completionRate = if (daysWithGoal.isEmpty()) 0f
            else daysWithGoal.count { it.metGoal(streakConfig.dailySuccessPercent) }.toFloat() / daysWithGoal.size

        val stats = LifetimeStats(
            totalEntries = entryRepository.totalEntryCount(),
            totalVolumeMl = entryRepository.totalVolumeMl(),
            currentDailyStreak = streak.currentDailyStreak,
            longestDailyStreak = streak.longestDailyStreak,
            weeklyStreak = streak.currentWeeklyStreak,
            goalCompletionRate = completionRate,
        )

        val result = evaluator.evaluate(current, stats)
        achievementRepository.upsertAll(result.updated)
        return result.newlyUnlocked
    }
}
