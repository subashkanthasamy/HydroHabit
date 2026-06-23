package com.bose.hydrohabit.presentation.achievements

import com.bose.hydrohabit.domain.engine.AchievementEvaluator
import com.bose.hydrohabit.domain.usecase.ObserveAchievementsUseCase
import com.bose.hydrohabit.presentation.mvi.MviStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * MVI store for the achievements grid. Falls back to the seeded catalog while the user has no
 * persisted achievement rows yet, so the full set always renders (locked, 0% progress).
 */
class AchievementsStore(
    scope: CoroutineScope,
    observeAchievements: ObserveAchievementsUseCase,
    private val evaluator: AchievementEvaluator,
) : MviStore<AchievementsState, AchievementsIntent> {

    private val _state = MutableStateFlow(AchievementsState())
    override val state: StateFlow<AchievementsState> = _state.asStateFlow()

    init {
        observeAchievements()
            .onEach { stored ->
                _state.value = AchievementsState(
                    isLoading = false,
                    achievements = stored.ifEmpty { evaluator.seedCatalog() },
                )
            }
            .launchIn(scope)
    }

    override fun dispatch(intent: AchievementsIntent) {
        // Reactive; nothing to reduce.
    }
}
