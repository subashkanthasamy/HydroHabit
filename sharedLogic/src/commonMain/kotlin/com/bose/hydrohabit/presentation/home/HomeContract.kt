package com.bose.hydrohabit.presentation.home

import com.bose.hydrohabit.domain.model.Achievement
import com.bose.hydrohabit.domain.model.DailyProgress
import com.bose.hydrohabit.domain.model.EntrySource
import com.bose.hydrohabit.domain.model.HydrationInsight
import com.bose.hydrohabit.domain.model.Streak
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.domain.usecase.QuickAddOption
import kotlinx.datetime.LocalDate

/** Immutable UI state for the home screen / dashboard. */
data class HomeState(
    val date: LocalDate? = null,
    val isLoading: Boolean = true,
    val progress: DailyProgress? = null,
    val streak: Streak = Streak.EMPTY,
    val quickAddOptions: List<QuickAddOption> = emptyList(),
    /** Most recent entries logged today (newest first) — the dashboard "Recent activity" feed. */
    val recentEntries: List<WaterEntry> = emptyList(),
    /** Personalized insights over the last week — the dashboard "Today's insights" cards. */
    val insights: List<HydrationInsight> = emptyList(),
    /** Achievements unlocked by the most recent log — consumed by the UI to show a celebration. */
    val newlyUnlocked: List<Achievement> = emptyList(),
    val error: String? = null,
)

/** User/UI intents the home store can reduce. */
sealed interface HomeIntent {
    data object Load : HomeIntent
    data class AddWater(val amountMl: Int, val source: EntrySource = EntrySource.CUSTOM) : HomeIntent
    data class AddQuickAdd(val option: QuickAddOption) : HomeIntent
    data object ClearUnlocked : HomeIntent
    data object ClearError : HomeIntent
}
