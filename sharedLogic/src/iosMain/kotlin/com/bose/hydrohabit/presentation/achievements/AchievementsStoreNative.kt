package com.bose.hydrohabit.presentation.achievements

import com.bose.hydrohabit.presentation.home.CancellationToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Swift-friendly facade over [AchievementsStore]. */
class AchievementsStoreNative(factory: AchievementsStoreFactory) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val store = factory.create(scope)

    val currentState: AchievementsState get() = store.state.value
    fun watch(onState: (AchievementsState) -> Unit): CancellationToken =
        CancellationToken(store.state.onEach { onState(it) }.launchIn(scope))

    fun dispatch(intent: AchievementsIntent) = store.dispatch(intent)
    fun close() = scope.cancel()
}
