package com.bose.hydrohabit.presentation.achievements

import com.bose.hydrohabit.domain.model.Achievement

data class AchievementsState(
    val isLoading: Boolean = true,
    val achievements: List<Achievement> = emptyList(),
) {
    val unlockedCount: Int get() = achievements.count { it.isUnlocked }
}

sealed interface AchievementsIntent {
    /** Reactive collection keeps state fresh; this is a no-op hook for parity with other stores. */
    data object Refresh : AchievementsIntent
}
