package com.bose.hydrohabit.domain.usecase

import com.bose.hydrohabit.domain.engine.AnalyticsCalculator
import com.bose.hydrohabit.domain.model.AnalyticsReport
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.HydrationInsight
import com.bose.hydrohabit.domain.model.ReportPeriod
import com.bose.hydrohabit.domain.repository.AnalyticsRepository

/** Assembles a DAILY/WEEKLY/MONTHLY report from the day-by-day rollups. */
class GenerateAnalyticsReportUseCase(
    private val analyticsRepository: AnalyticsRepository,
    private val calculator: AnalyticsCalculator,
) {
    suspend operator fun invoke(
        period: ReportPeriod,
        range: DateRange,
        successThreshold: Float = 1.0f,
    ): AnalyticsReport {
        val summaries = analyticsRepository.getDailySummaries(range)
        return calculator.buildReport(period, range, summaries, successThreshold)
    }
}

/** Standalone insights for a range (e.g. a home-screen tip card). */
class GetHydrationInsightsUseCase(
    private val analyticsRepository: AnalyticsRepository,
    private val calculator: AnalyticsCalculator,
) {
    suspend operator fun invoke(range: DateRange, successThreshold: Float = 1.0f): List<HydrationInsight> {
        val summaries = analyticsRepository.getDailySummaries(range)
        val daysWithGoal = summaries.filter { it.goalMl > 0 }
        val completionRate = if (daysWithGoal.isEmpty()) 0f
            else daysWithGoal.count { it.metGoal(successThreshold) }.toFloat() / daysWithGoal.size
        return calculator.buildInsights(summaries, completionRate, successThreshold)
    }
}
