package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.InsightType
import com.bose.hydrohabit.domain.model.ReportPeriod
import com.bose.hydrohabit.testutil.FixedTimeProvider
import com.bose.hydrohabit.testutil.SequentialIdGenerator
import com.bose.hydrohabit.testutil.daySummary
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsCalculatorTest {

    private val calculator = AnalyticsCalculator(
        FixedTimeProvider(Instant.fromEpochMilliseconds(0)),
        SequentialIdGenerator("ins"),
    )
    private val today = LocalDate(2026, 6, 18)
    private fun daysAgo(n: Int) = today.minus(n, DateTimeUnit.DAY)
    private val range = DateRange(daysAgo(6), today)

    @Test
    fun computesTotalsAverageAndCompletion() {
        val summaries = listOf(
            daySummary(daysAgo(2), consumedMl = 2000, goalMl = 2000),
            daySummary(daysAgo(1), consumedMl = 1000, goalMl = 2000),
            daySummary(today, consumedMl = 3000, goalMl = 2000),
        )
        val report = calculator.buildReport(ReportPeriod.WEEKLY, range, summaries)
        assertEquals(6000L, report.totalMl)
        assertEquals(2000, report.averageMl)
        // 2 of 3 days met goal (2000 and 3000)
        assertEquals(2f / 3f, report.goalCompletionRate)
    }

    @Test
    fun identifiesBestAndWorstDay() {
        val summaries = listOf(
            daySummary(daysAgo(2), consumedMl = 2500),
            daySummary(daysAgo(1), consumedMl = 800),
            daySummary(today, consumedMl = 1500),
        )
        val report = calculator.buildReport(ReportPeriod.WEEKLY, range, summaries)
        assertEquals(daysAgo(2), report.bestDay?.date)
        assertEquals(daysAgo(1), report.worstDay?.date)
    }

    @Test
    fun emptyInputProducesEmptyReport() {
        val report = calculator.buildReport(ReportPeriod.DAILY, range, emptyList())
        assertEquals(0L, report.totalMl)
        assertEquals(0, report.averageMl)
        assertEquals(0f, report.goalCompletionRate)
        assertTrue(report.insights.isEmpty())
    }

    @Test
    fun highCompletionYieldsPositiveInsight() {
        val summaries = (0..6).map { daySummary(daysAgo(it), consumedMl = 2100, goalMl = 2000) }
        val report = calculator.buildReport(ReportPeriod.WEEKLY, range, summaries)
        assertTrue(report.insights.any { it.type == InsightType.POSITIVE })
    }

    @Test
    fun upwardTrendProducesTrendInsight() {
        val summaries = listOf(
            daySummary(daysAgo(3), consumedMl = 1000, goalMl = 2000),
            daySummary(daysAgo(2), consumedMl = 1000, goalMl = 2000),
            daySummary(daysAgo(1), consumedMl = 2000, goalMl = 2000),
            daySummary(today, consumedMl = 2000, goalMl = 2000),
        )
        val report = calculator.buildReport(ReportPeriod.WEEKLY, range, summaries)
        assertTrue(report.insights.any { it.type == InsightType.TREND })
    }
}
