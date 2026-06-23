package com.bose.hydrohabit.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.data.mapper.toDomain
import com.bose.hydrohabit.db.HydroHabitDatabase
import com.bose.hydrohabit.domain.model.Streak
import com.bose.hydrohabit.domain.repository.StreakRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StreakRepositoryImpl(
    db: HydroHabitDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : StreakRepository {

    private val queries = db.streakQueries

    override fun observeStreak(): Flow<Streak> =
        queries.selectStreak().asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() ?: Streak.EMPTY }

    override suspend fun getStreak(): Streak = withContext(dispatcher) {
        queries.selectStreak().executeAsOneOrNull()?.toDomain() ?: Streak.EMPTY
    }

    override suspend fun updateStreak(streak: Streak): AppResult<Unit> = dbWrite(dispatcher) {
        queries.upsertStreak(
            currentDailyStreak = streak.currentDailyStreak.toLong(),
            longestDailyStreak = streak.longestDailyStreak.toLong(),
            currentWeeklyStreak = streak.currentWeeklyStreak.toLong(),
            currentMonthlyStreak = streak.currentMonthlyStreak.toLong(),
            lastQualifyingDate = streak.lastQualifyingDate?.toString(),
        )
    }
}
