package com.bose.hydrohabit.domain.model

import kotlinx.datetime.LocalDate

/**
 * A computed report over a date range. Assembled by `GenerateAnalyticsReportUseCase` from
 * [DaySummary] rows + insights.
 */
data class AnalyticsReport(
    val period: ReportPeriod,
    val range: DateRange,
    val totalMl: Long,
    val averageMl: Int,
    /** 0f..1f — share of days in range that met their goal. */
    val goalCompletionRate: Float,
    val bestDay: DaySummary?,
    val worstDay: DaySummary?,
    val dailyBreakdown: List<DaySummary>,
    val insights: List<HydrationInsight>,
)

enum class ReportPeriod { DAILY, WEEKLY, MONTHLY }

/** Per-day rollup; the unit of analytics. [goalMl] may be 0 for days with no goal set. */
data class DaySummary(
    val date: LocalDate,
    val consumedMl: Int,
    val goalMl: Int,
    val entryCount: Int,
) {
    val completionPercent: Float
        get() = if (goalMl <= 0) 0f else (consumedMl.toFloat() / goalMl).coerceIn(0f, 1f)

    fun metGoal(threshold: Float = 1.0f): Boolean = goalMl > 0 && completionPercent >= threshold
}

/** Inclusive [start, end] date range. */
data class DateRange(val start: LocalDate, val end: LocalDate) {
    init { require(start <= end) { "start ($start) must be <= end ($end)" } }
}
