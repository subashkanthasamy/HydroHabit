package com.bose.hydrohabit.presentation.analytics

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.ReportPeriod
import com.bose.hydrohabit.domain.usecase.GenerateAnalyticsReportUseCase
import com.bose.hydrohabit.presentation.mvi.MviStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** MVI store for analytics. Recomputes the report whenever the selected period changes. */
class AnalyticsStore(
    private val scope: CoroutineScope,
    private val generateReport: GenerateAnalyticsReportUseCase,
    private val timeProvider: TimeProvider,
) : MviStore<AnalyticsState, AnalyticsIntent> {

    private val _state = MutableStateFlow(AnalyticsState())
    override val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        load(_state.value.period)
    }

    override fun dispatch(intent: AnalyticsIntent) {
        when (intent) {
            is AnalyticsIntent.SelectPeriod -> load(intent.period)
            AnalyticsIntent.Refresh -> load(_state.value.period)
        }
    }

    private fun load(period: ReportPeriod) {
        _state.value = _state.value.copy(period = period, isLoading = true)
        scope.launch {
            val report = generateReport(period, rangeFor(period))
            _state.value = _state.value.copy(report = report, isLoading = false)
        }
    }

    private fun rangeFor(period: ReportPeriod): DateRange {
        val today = timeProvider.today()
        return when (period) {
            ReportPeriod.DAILY -> DateRange(today, today)
            ReportPeriod.WEEKLY -> DateRange(today.minus(6, DateTimeUnit.DAY), today)
            ReportPeriod.MONTHLY -> DateRange(LocalDate(today.year, today.month, 1), today)
        }
    }
}
