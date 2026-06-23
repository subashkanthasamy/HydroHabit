package com.bose.hydrohabit.presentation.home

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.usecase.AddWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.EvaluateAchievementsUseCase
import com.bose.hydrohabit.domain.usecase.GetHydrationInsightsUseCase
import com.bose.hydrohabit.domain.usecase.GetQuickAddOptionsUseCase
import com.bose.hydrohabit.domain.usecase.GetUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.ObserveDailyProgressUseCase
import com.bose.hydrohabit.domain.usecase.ObserveEntriesForDateUseCase
import com.bose.hydrohabit.domain.usecase.ObserveStreakUseCase
import com.bose.hydrohabit.domain.usecase.RecalculateStreakUseCase
import com.bose.hydrohabit.domain.usecase.RescheduleRemindersUseCase
import kotlinx.coroutines.CoroutineScope

/**
 * Builds a [HomeStore] for a given lifecycle [CoroutineScope]. The store can't be a DI singleton
 * (its lifetime is tied to a screen's scope), so this factory is the singleton instead — Android
 * passes `viewModelScope`, iOS passes a view-tied scope.
 */
class HomeStoreFactory(
    private val observeDailyProgress: ObserveDailyProgressUseCase,
    private val observeStreak: ObserveStreakUseCase,
    private val getUserProfile: GetUserProfileUseCase,
    private val getQuickAddOptions: GetQuickAddOptionsUseCase,
    private val observeEntriesForDate: ObserveEntriesForDateUseCase,
    private val addWaterEntry: AddWaterEntryUseCase,
    private val evaluateAchievements: EvaluateAchievementsUseCase,
    private val recalculateStreak: RecalculateStreakUseCase,
    private val rescheduleReminders: RescheduleRemindersUseCase,
    private val getHydrationInsights: GetHydrationInsightsUseCase,
    private val timeProvider: TimeProvider,
) {
    fun create(scope: CoroutineScope): HomeStore = HomeStore(
        scope = scope,
        observeDailyProgress = observeDailyProgress,
        observeStreak = observeStreak,
        getUserProfile = getUserProfile,
        getQuickAddOptions = getQuickAddOptions,
        observeEntriesForDate = observeEntriesForDate,
        addWaterEntry = addWaterEntry,
        evaluateAchievements = evaluateAchievements,
        recalculateStreak = recalculateStreak,
        rescheduleReminders = rescheduleReminders,
        getHydrationInsights = getHydrationInsights,
        timeProvider = timeProvider,
    )
}
