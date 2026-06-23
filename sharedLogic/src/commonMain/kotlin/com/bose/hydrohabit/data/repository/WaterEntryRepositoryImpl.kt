package com.bose.hydrohabit.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.data.mapper.toDomain
import com.bose.hydrohabit.db.HydroHabitDatabase
import com.bose.hydrohabit.domain.model.DateRange
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.domain.repository.WaterEntryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class WaterEntryRepositoryImpl(
    db: HydroHabitDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : WaterEntryRepository {

    private val queries = db.waterEntryQueries

    override fun observeEntries(date: LocalDate): Flow<List<WaterEntry>> =
        queries.selectByDate(date.toString()).asFlow().mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeEntriesInRange(range: DateRange): Flow<List<WaterEntry>> =
        queries.selectInRange(range.start.toString(), range.end.toString()).asFlow().mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun totalForDate(date: LocalDate): Int = withContext(dispatcher) {
        queries.totalForDate(date.toString()).executeAsOne().toInt()
    }

    override suspend fun addEntry(entry: WaterEntry): AppResult<Unit> = dbWrite(dispatcher) {
        queries.insertEntry(
            id = entry.id,
            amountMl = entry.amountMl.toLong(),
            timestamp = entry.timestamp.toEpochMilliseconds(),
            date = entry.date.toString(),
            source = entry.source.name,
            note = entry.note,
        )
    }

    override suspend fun updateEntry(entry: WaterEntry): AppResult<Unit> = addEntry(entry) // INSERT OR REPLACE

    override suspend fun deleteEntry(id: String): AppResult<Unit> = dbWrite(dispatcher) {
        queries.deleteEntry(id)
    }

    override suspend fun totalEntryCount(): Int = withContext(dispatcher) {
        queries.totalCount().executeAsOne().toInt()
    }

    override suspend fun totalVolumeMl(): Long = withContext(dispatcher) {
        queries.totalVolume().executeAsOne()
    }
}
