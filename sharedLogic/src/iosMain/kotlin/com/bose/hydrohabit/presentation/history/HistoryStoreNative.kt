package com.bose.hydrohabit.presentation.history

import com.bose.hydrohabit.presentation.home.CancellationToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Swift-friendly facade over [HistoryStore]. See `HomeStoreNative` for the pattern. */
class HistoryStoreNative(factory: HistoryStoreFactory) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val store = factory.create(scope)

    val currentState: HistoryState get() = store.state.value
    fun watch(onState: (HistoryState) -> Unit): CancellationToken =
        CancellationToken(store.state.onEach { onState(it) }.launchIn(scope))

    fun dispatch(intent: HistoryIntent) = store.dispatch(intent)

    // Primitive-friendly helpers so Swift needn't construct Kotlin sealed types.
    fun previousDay() = store.dispatch(HistoryIntent.PreviousDay)
    fun nextDay() = store.dispatch(HistoryIntent.NextDay)
    fun delete(id: String) = store.dispatch(HistoryIntent.DeleteEntry(id))

    fun close() = scope.cancel()
}
