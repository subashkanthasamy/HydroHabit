package com.bose.hydrohabit.presentation.analytics

import com.bose.hydrohabit.domain.model.ReportPeriod
import com.bose.hydrohabit.presentation.home.CancellationToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Swift-friendly facade over [AnalyticsStore]. */
class AnalyticsStoreNative(factory: AnalyticsStoreFactory) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val store = factory.create(scope)

    val currentState: AnalyticsState get() = store.state.value
    fun watch(onState: (AnalyticsState) -> Unit): CancellationToken =
        CancellationToken(store.state.onEach { onState(it) }.launchIn(scope))

    fun dispatch(intent: AnalyticsIntent) = store.dispatch(intent)
    fun selectPeriod(period: ReportPeriod) = store.dispatch(AnalyticsIntent.SelectPeriod(period))

    fun close() = scope.cancel()
}
