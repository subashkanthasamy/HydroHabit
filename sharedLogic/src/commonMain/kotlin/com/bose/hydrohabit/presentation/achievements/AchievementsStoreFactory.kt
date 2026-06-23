package com.bose.hydrohabit.presentation.achievements

import com.bose.hydrohabit.domain.engine.AchievementEvaluator
import com.bose.hydrohabit.domain.usecase.ObserveAchievementsUseCase
import kotlinx.coroutines.CoroutineScope

class AchievementsStoreFactory(
    private val observeAchievements: ObserveAchievementsUseCase,
    private val evaluator: AchievementEvaluator,
) {
    fun create(scope: CoroutineScope): AchievementsStore =
        AchievementsStore(scope, observeAchievements, evaluator)
}
