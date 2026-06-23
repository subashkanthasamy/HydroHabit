package com.bose.hydrohabit.presentation.home

import com.bose.hydrohabit.domain.usecase.CreateInitialProfileUseCase
import com.bose.hydrohabit.domain.usecase.QuickAddOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** Cancels a [HomeStoreNative.watch] subscription. */
class CancellationToken(private val job: Job) {
    fun cancel() = job.cancel()
}

/**
 * Swift-friendly facade over [HomeStore]. Swift can't construct a Kotlin `CoroutineScope` or
 * collect a `StateFlow` directly, so this owns the scope and exposes callback-based [watch] plus
 * plain dispatch methods. Created via `KoinHelper.createHomeStore()`.
 */
class HomeStoreNative(
    factory: HomeStoreFactory,
    private val createInitialProfile: CreateInitialProfileUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val store = factory.create(scope)

    val currentState: HomeState get() = store.state.value

    fun watch(onState: (HomeState) -> Unit): CancellationToken {
        val job = store.state.onEach { onState(it) }.launchIn(scope)
        return CancellationToken(job)
    }

    fun addQuickAdd(option: QuickAddOption) = store.dispatch(HomeIntent.AddQuickAdd(option))
    fun addWater(amountMl: Int) = store.dispatch(HomeIntent.AddWater(amountMl))
    fun clearUnlocked() = store.dispatch(HomeIntent.ClearUnlocked)

    fun createProfile(weightKg: Double, age: Int) {
        scope.launch { createInitialProfile(weightKg, age) }
    }

    fun close() {
        scope.cancel()
    }
}
