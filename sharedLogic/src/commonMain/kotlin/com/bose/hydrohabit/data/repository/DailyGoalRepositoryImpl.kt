package com.bose.hydrohabit.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.data.mapper.toDomain
import com.bose.hydrohabit.db.HydroHabitDatabase
import com.bose.hydrohabit.domain.model.DailyGoal
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.repository.DailyGoalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class DailyGoalRepositoryImpl(
    db: HydroHabitDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : DailyGoalRepository {

    private val queries = db.dailyGoalQueries

    override fun observeGoal(date: LocalDate): Flow<DailyGoal?> =
        queries.selectByDate(date.toString()).asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    override suspend fun getGoal(date: LocalDate): DailyGoal? = withContext(dispatcher) {
        queries.selectByDate(date.toString()).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun upsertGoal(goal: DailyGoal): AppResult<Unit> = dbWrite(dispatcher) {
        queries.upsertGoal(
            date = goal.date.toString(),
            targetMl = goal.targetMl.toLong(),
            source = goal.source.name,
            baseMl = goal.baseMl.toLong(),
            activityAdjustmentMl = goal.activityAdjustmentMl.toLong(),
            weatherAdjustmentMl = goal.weatherAdjustmentMl.toLong(),
        )
    }

    override suspend fun getGoalsInRange(range: DateRange): List<DailyGoal> = withContext(dispatcher) {
        queries.selectInRange(range.start.toString(), range.end.toString()).executeAsList().map { it.toDomain() }
    }
}
