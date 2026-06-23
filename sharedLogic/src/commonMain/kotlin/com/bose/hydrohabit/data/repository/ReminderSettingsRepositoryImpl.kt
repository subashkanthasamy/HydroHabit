package com.bose.hydrohabit.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.data.mapper.encodeQuietWindows
import com.bose.hydrohabit.data.mapper.toDomain
import com.bose.hydrohabit.db.HydroHabitDatabase
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.repository.ReminderSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ReminderSettingsRepositoryImpl(
    db: HydroHabitDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ReminderSettingsRepository {

    private val queries = db.reminderSettingsQueries

    override fun observeSettings(): Flow<ReminderSettings> =
        queries.selectSettings().asFlow().mapToOneOrNull(dispatcher)
            .map { it?.toDomain() ?: ReminderSettings.DEFAULT }

    override suspend fun getSettings(): ReminderSettings = withContext(dispatcher) {
        queries.selectSettings().executeAsOneOrNull()?.toDomain() ?: ReminderSettings.DEFAULT
    }

    override suspend fun updateSettings(settings: ReminderSettings): AppResult<Unit> = dbWrite(dispatcher) {
        queries.upsertSettings(
            enabled = if (settings.enabled) 1L else 0L,
            intervalMinutes = settings.intervalMinutes.toLong(),
            wakeTime = settings.wakeTime.toString(),
            sleepTime = settings.sleepTime.toString(),
            strategy = settings.strategy.name,
            quietWindowsJson = encodeQuietWindows(settings.quietWindows),
            skipIfRecentlyLoggedMinutes = settings.skipIfRecentlyLoggedMinutes.toLong(),
            soundEnabled = if (settings.soundEnabled) 1L else 0L,
            vibrationEnabled = if (settings.vibrationEnabled) 1L else 0L,
        )
    }
}
