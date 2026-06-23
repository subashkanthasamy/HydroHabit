package com.bose.hydrohabit.presentation.analytics

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.usecase.GenerateAnalyticsReportUseCase
import kotlinx.coroutines.CoroutineScope

class AnalyticsStoreFactory(
    private val generateReport: GenerateAnalyticsReportUseCase,
    private val timeProvider: TimeProvider,
) {
    fun create(scope: CoroutineScope): AnalyticsStore =
        AnalyticsStore(scope, generateReport, timeProvider)
}
