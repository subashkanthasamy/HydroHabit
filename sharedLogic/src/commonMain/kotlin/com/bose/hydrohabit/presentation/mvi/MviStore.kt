package com.bose.hydrohabit.presentation.mvi

import kotlinx.coroutines.flow.StateFlow

/**
 * Minimal MVI contract shared by every feature store.
 *
 *  - [state] is the single source of UI truth (a reactive [StateFlow]).
 *  - [dispatch] is the only way to mutate it — UI sends intents, the store reduces them.
 *
 * Stores hold a [kotlinx.coroutines.CoroutineScope] injected by the platform (Android
 * `viewModelScope`, iOS a scope tied to the view lifecycle), so this layer stays free of any
 * platform ViewModel dependency while remaining MVVM/MVI-compatible.
 */
interface MviStore<S, I> {
    val state: StateFlow<S>
    fun dispatch(intent: I)
}
