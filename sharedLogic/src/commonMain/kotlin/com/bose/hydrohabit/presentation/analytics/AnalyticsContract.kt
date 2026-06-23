package com.bose.hydrohabit.presentation.analytics

import com.bose.hydrohabit.domain.model.AnalyticsReport
import com.bose.hydrohabit.domain.model.ReportPeriod

data class AnalyticsState(
    val period: ReportPeriod = ReportPeriod.WEEKLY,
    val isLoading: Boolean = true,
    val report: AnalyticsReport? = null,
)

sealed interface AnalyticsIntent {
    data class SelectPeriod(val period: ReportPeriod) : AnalyticsIntent
    data object Refresh : AnalyticsIntent
}
