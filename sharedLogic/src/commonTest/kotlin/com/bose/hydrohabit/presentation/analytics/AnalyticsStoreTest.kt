package com.bose.hydrohabit.presentation.analytics

import com.bose.hydrohabit.domain.engine.AnalyticsCalculator
import com.bose.hydrohabit.domain.model.ReportPeriod
import com.bose.hydrohabit.domain.usecase.GenerateAnalyticsReportUseCase
import com.bose.hydrohabit.testutil.FakeAnalyticsRepository
import com.bose.hydrohabit.testutil.FixedTimeProvider
import com.bose.hydrohabit.testutil.SequentialIdGenerator
import com.bose.hydrohabit.testutil.daySummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsStoreTest {

    private val today = LocalDate(2026, 6, 18)
    // FixedTimeProvider needs an instant; midday today is fine for date resolution.
    private val now = Instant.parse("2026-06-18T12:00:00Z")

    @Test
    fun loadsWeeklyReportOnInit() = runTest {
        val repo = FakeAnalyticsRepository(
            listOf(
                daySummary(today, consumedMl = 2000, goalMl = 2000),
                daySummary(today.minus(1, DateTimeUnit.DAY), consumedMl = 1500, goalMl = 2000),
            ),
        )
        val store = AnalyticsStore(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            GenerateAnalyticsReportUseCase(repo, AnalyticsCalculator(FixedTimeProvider(now), SequentialIdGenerator("i"))),
            FixedTimeProvider(now),
        )

        val report = store.state.value.report
        assertNotNull(report)
        assertEquals(ReportPeriod.WEEKLY, store.state.value.period)
        assertEquals(3500L, report.totalMl)
    }

    @Test
    fun switchingToDailyNarrowsTheRange() = runTest {
        val repo = FakeAnalyticsRepository(
            listOf(
                daySummary(today, consumedMl = 2000, goalMl = 2000),
                daySummary(today.minus(1, DateTimeUnit.DAY), consumedMl = 1500, goalMl = 2000),
            ),
        )
        val store = AnalyticsStore(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            GenerateAnalyticsReportUseCase(repo, AnalyticsCalculator(FixedTimeProvider(now), SequentialIdGenerator("i"))),
            FixedTimeProvider(now),
        )

        store.dispatch(AnalyticsIntent.SelectPeriod(ReportPeriod.DAILY))
        assertEquals(2000L, store.state.value.report?.totalMl)
    }
}
