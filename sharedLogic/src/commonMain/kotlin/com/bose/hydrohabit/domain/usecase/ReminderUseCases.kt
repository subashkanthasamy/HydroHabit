package com.bose.hydrohabit.domain.usecase

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.engine.ReminderScheduler
import com.bose.hydrohabit.domain.model.DailyProgress
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.ScheduledReminder
import com.bose.hydrohabit.domain.reminders.PlatformReminderScheduler
import com.bose.hydrohabit.domain.repository.DailyGoalRepository
import com.bose.hydrohabit.domain.repository.ReminderSettingsRepository
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.domain.repository.WaterEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GetReminderSettingsUseCase(private val repository: ReminderSettingsRepository) {
    operator fun invoke(): Flow<ReminderSettings> = repository.observeSettings()
}

class UpdateReminderSettingsUseCase(private val repository: ReminderSettingsRepository) {
    suspend operator fun invoke(settings: ReminderSettings): AppResult<Unit> =
        repository.updateSettings(settings)
}

/**
 * Builds today's reminder plan from current settings + live progress + recent activity, using the
 * pure [ReminderScheduler]. Pure logic stays testable; this use case just gathers inputs.
 */
class ComputeReminderScheduleUseCase(
    private val settingsRepository: ReminderSettingsRepository,
    private val goalRepository: DailyGoalRepository,
    private val entryRepository: WaterEntryRepository,
    private val scheduler: ReminderScheduler,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(): List<ScheduledReminder> {
        val settings = settingsRepository.getSettings()
        val today = timeProvider.today()
        val goal = goalRepository.getGoal(today)?.targetMl ?: 0
        // One-shot read of today's entries to compute progress + last log time.
        val entries: List<WaterEntry> = entryRepository.observeEntries(today).firstOrNull().orEmpty()
        val progress = DailyProgress(
            date = today,
            goalMl = goal,
            consumedMl = entries.sumOf { it.amountMl },
            entryCount = entries.size,
            lastEntryAt = entries.maxByOrNull { it.timestamp }?.timestamp,
        )
        return scheduler.plan(
            settings = settings,
            progress = progress,
            now = timeProvider.now(),
            zone = timeProvider.timeZone(),
            lastEntryAt = entries.maxByOrNull { it.timestamp }?.timestamp,
        )
    }
}

/**
 * Recomputes the plan and hands it to the platform scheduler. Called when settings change, a log
 * is added, the day rolls over, or a reminder fires — keeping the adaptive schedule honest.
 */
class RescheduleRemindersUseCase(
    private val computeSchedule: ComputeReminderScheduleUseCase,
    private val settingsRepository: ReminderSettingsRepository,
    private val platformScheduler: PlatformReminderScheduler,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val settings = settingsRepository.getSettings()
        if (!settings.enabled) {
            platformScheduler.cancelAll()
            return AppResult.Success(Unit)
        }
        val plan = computeSchedule()
        platformScheduler.schedule(plan)
        return AppResult.Success(Unit)
    }
}
