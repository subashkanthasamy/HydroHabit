package com.bose.hydrohabit.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bose.hydrohabit.db.DailyGoalEntity
import com.bose.hydrohabit.db.DailySummaries
import com.bose.hydrohabit.db.HydroHabitDatabase
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.DaySummary
import com.bose.hydrohabit.domain.repository.AnalyticsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class AnalyticsRepositoryImpl(
    db: HydroHabitDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AnalyticsRepository {

    private val analyticsQueries = db.analyticsQueries
    private val goalQueries = db.dailyGoalQueries

    override fun observeDailySummaries(range: DateRange): Flow<List<DaySummary>> =
        combine(
            analyticsQueries.dailySummaries(range.start.toString(), range.end.toString())
                .asFlow().mapToList(dispatcher),
            goalQueries.selectInRange(range.start.toString(), range.end.toString())
                .asFlow().mapToList(dispatcher),
        ) { summaries, goals -> merge(summaries, goals) }

    override suspend fun getDailySummaries(range: DateRange): List<DaySummary> = withContext(dispatcher) {
        val summaries = analyticsQueries
            .dailySummaries(range.start.toString(), range.end.toString()).executeAsList()
        val goals = goalQueries
            .selectInRange(range.start.toString(), range.end.toString()).executeAsList()
        merge(summaries, goals)
    }

    /**
     * Combine the entries-based aggregate with goal rows so that days that have a goal but no
     * logged water still appear (as 0-consumption days) — important for accurate completion rates.
     */
    private fun merge(summaries: List<DailySummaries>, goals: List<DailyGoalEntity>): List<DaySummary> {
        val byDate = summaries.associate { row ->
            row.date to DaySummary(
                date = LocalDate.parse(row.date),
                consumedMl = (row.consumedMl ?: 0L).toInt(),
                goalMl = row.goalMl.toInt(),
                entryCount = row.entryCount.toInt(),
            )
        }.toMutableMap()

        goals.forEach { goal ->
            byDate.getOrPut(goal.date) {
                DaySummary(
                    date = LocalDate.parse(goal.date),
                    consumedMl = 0,
                    goalMl = goal.targetMl.toInt(),
                    entryCount = 0,
                )
            }
        }
        return byDate.values.sortedBy { it.date }
    }
}
