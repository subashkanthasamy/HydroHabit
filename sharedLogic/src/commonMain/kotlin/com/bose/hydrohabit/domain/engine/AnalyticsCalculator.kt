package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.core.util.IdGenerator
import com.bose.hydrohabit.domain.model.AnalyticsReport
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.DaySummary
import com.bose.hydrohabit.domain.model.HydrationInsight
import com.bose.hydrohabit.domain.model.InsightType
import com.bose.hydrohabit.domain.model.ReportPeriod
import kotlin.math.roundToInt

/**
 * Pure aggregation of [DaySummary] rows into an [AnalyticsReport] plus personalized insights.
 * Averages are taken over the days present in the input (already filtered to the range upstream).
 */
class AnalyticsCalculator(
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
) {
    fun buildReport(
        period: ReportPeriod,
        range: DateRange,
        summaries: List<DaySummary>,
        successThreshold: Float = 1.0f,
    ): AnalyticsReport {
        val daysWithData = summaries.filter { it.entryCount > 0 || it.consumedMl > 0 }
        val totalMl = summaries.sumOf { it.consumedMl.toLong() }
        val averageMl = if (daysWithData.isEmpty()) 0 else (totalMl.toDouble() / daysWithData.size).roundToInt()

        val daysWithGoal = summaries.filter { it.goalMl > 0 }
        val completionRate = if (daysWithGoal.isEmpty()) 0f
            else daysWithGoal.count { it.metGoal(successThreshold) }.toFloat() / daysWithGoal.size

        return AnalyticsReport(
            period = period,
            range = range,
            totalMl = totalMl,
            averageMl = averageMl,
            goalCompletionRate = completionRate,
            bestDay = daysWithData.maxByOrNull { it.consumedMl },
            worstDay = daysWithData.minByOrNull { it.consumedMl },
            dailyBreakdown = summaries.sortedBy { it.date },
            insights = buildInsights(summaries, completionRate, successThreshold),
        )
    }

    /** Derive human-readable insights. Deterministic given inputs + injected time/id. */
    fun buildInsights(
        summaries: List<DaySummary>,
        completionRate: Float,
        successThreshold: Float = 1.0f,
    ): List<HydrationInsight> {
        if (summaries.isEmpty()) return emptyList()
        val now = timeProvider.now()
        val insights = mutableListOf<HydrationInsight>()

        fun add(type: InsightType, message: String, metric: Double? = null) =
            insights.add(HydrationInsight(idGenerator.newId(), type, message, metric, now))

        when {
            completionRate >= 0.8f ->
                add(InsightType.POSITIVE, "Great consistency — you met your goal on most days.", completionRate.toDouble())
            completionRate <= 0.4f ->
                add(InsightType.WARNING, "You're below your goal on most days. Try smaller, more frequent sips.", completionRate.toDouble())
        }

        // Trend: compare the first vs second half of the period.
        val sorted = summaries.sortedBy { it.date }
        if (sorted.size >= 4) {
            val mid = sorted.size / 2
            val firstAvg = sorted.take(mid).map { it.consumedMl }.average()
            val secondAvg = sorted.drop(mid).map { it.consumedMl }.average()
            if (firstAvg > 0) {
                val changePct = ((secondAvg - firstAvg) / firstAvg * 100).roundToInt()
                when {
                    changePct >= 10 -> add(InsightType.TREND, "Your intake is trending up (+$changePct%) — keep it going!", changePct.toDouble())
                    changePct <= -10 -> add(InsightType.TREND, "Your intake dropped ($changePct%) recently. Let's bounce back.", changePct.toDouble())
                }
            }
        }

        if (insights.none { it.type == InsightType.POSITIVE }) {
            add(InsightType.TIP, "Keep a bottle within reach — visibility is the easiest hydration nudge.")
        }
        return insights
    }
}
