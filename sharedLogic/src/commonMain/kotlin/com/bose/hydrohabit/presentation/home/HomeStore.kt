package com.bose.hydrohabit.presentation.home

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.model.DailyProgress
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.EntrySource
import com.bose.hydrohabit.domain.model.Streak
import com.bose.hydrohabit.domain.model.UnitSystem
import com.bose.hydrohabit.domain.model.UserProfile
import com.bose.hydrohabit.domain.model.WaterEntry
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
import com.bose.hydrohabit.presentation.mvi.MviStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

/**
 * MVI store backing the dashboard. Combines the reactive progress / streak / profile / entries
 * feeds into [HomeState] and reduces logging intents into use-case calls (which ripple back through
 * the same flows). Insights are recomputed when a goal first appears and after each log.
 */
class HomeStore(
    private val scope: CoroutineScope,
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
) : MviStore<HomeState, HomeIntent> {

    private val _state = MutableStateFlow(HomeState())
    override val state: StateFlow<HomeState> = _state.asStateFlow()

    // Tracks the last seen goal so we can (re)schedule + refresh insights the moment a goal first
    // appears (e.g. right after first-run profile setup) on any platform.
    private var lastGoalMl = 0

    init {
        dispatch(HomeIntent.Load)
        scope.launch { rescheduleReminders() }
    }

    override fun dispatch(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Load -> load()
            is HomeIntent.AddWater -> logWater(intent.amountMl, intent.source)
            is HomeIntent.AddQuickAdd -> logWater(intent.option.amountMl, EntrySource.QUICK_ADD)
            HomeIntent.ClearUnlocked -> _state.update { it.copy(newlyUnlocked = emptyList()) }
            HomeIntent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun load() {
        val today = timeProvider.today()
        _state.update { it.copy(date = today, isLoading = true) }
        combine(
            observeDailyProgress(today),
            observeStreak(),
            getUserProfile(),
            observeEntriesForDate(today),
        ) { progress, streak, profile, entries ->
            ProgressData(progress, streak, profile, entries)
        }.onEach { data ->
            val units = data.profile?.unitSystem ?: UnitSystem.METRIC
            _state.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    progress = data.progress,
                    streak = data.streak,
                    quickAddOptions = getQuickAddOptions(units),
                    recentEntries = data.entries.sortedByDescending { it.timestamp }.take(RECENT_LIMIT),
                )
            }
            val goal = data.progress?.goalMl ?: 0
            if (goal > 0 && lastGoalMl == 0) {
                rescheduleReminders()
                refreshInsights()
            }
            lastGoalMl = goal
        }.launchIn(scope)
    }

    private fun logWater(amountMl: Int, source: EntrySource) {
        scope.launch {
            when (val result = addWaterEntry(amountMl, source)) {
                is AppResult.Failure -> _state.update { it.copy(error = result.error.message) }
                is AppResult.Success -> {
                    // Recompute streak first so achievement evaluation sees the latest streak.
                    recalculateStreak()
                    val unlocked = evaluateAchievements()
                    if (unlocked.isNotEmpty()) {
                        _state.update { it.copy(newlyUnlocked = it.newlyUnlocked + unlocked) }
                    }
                    // Logging changes remaining goal + recent activity → re-plan reminders (adaptive
                    // skip/catch-up) and refresh insights.
                    rescheduleReminders()
                    refreshInsights()
                }
            }
        }
    }

    private fun refreshInsights() {
        scope.launch {
            val today = timeProvider.today()
            val range = DateRange(today.minus(INSIGHT_WINDOW_DAYS, DateTimeUnit.DAY), today)
            val insights = getHydrationInsights(range)
            _state.update { it.copy(insights = insights) }
        }
    }

    companion object {
        private const val RECENT_LIMIT = 5
        private const val INSIGHT_WINDOW_DAYS = 6
    }
}

private data class ProgressData(
    val progress: DailyProgress?,
    val streak: Streak,
    val profile: UserProfile?,
    val entries: List<WaterEntry>
)
