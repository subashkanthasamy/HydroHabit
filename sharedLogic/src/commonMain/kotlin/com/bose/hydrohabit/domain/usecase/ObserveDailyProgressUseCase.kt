package com.bose.hydrohabit.domain.usecase

import com.bose.hydrohabit.domain.model.DailyProgress
import com.bose.hydrohabit.domain.repository.DailyGoalRepository
import com.bose.hydrohabit.domain.repository.WaterEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

/**
 * Reactive [DailyProgress] for [date] — recomputed whenever the goal or the day's entries change.
 * This is the primary feed for the home screen.
 */
class ObserveDailyProgressUseCase(
    private val goalRepository: DailyGoalRepository,
    private val entryRepository: WaterEntryRepository,
) {
    operator fun invoke(date: LocalDate): Flow<DailyProgress> =
        combine(
            goalRepository.observeGoal(date),
            entryRepository.observeEntries(date),
        ) { goal, entries ->
            DailyProgress(
                date = date,
                goalMl = goal?.targetMl ?: 0,
                consumedMl = entries.sumOf { it.amountMl },
                entryCount = entries.size,
                lastEntryAt = entries.maxByOrNull { it.timestamp }?.timestamp,
            )
        }
}
