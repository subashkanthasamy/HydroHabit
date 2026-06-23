package com.bose.hydrohabit.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.usecase.CalculateDailyGoalUseCase
import com.bose.hydrohabit.domain.usecase.RescheduleRemindersUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Recomputes today's goal (creating the per-date goal row if the day just rolled over) and then
 * re-arms reminders. Runs daily via WorkManager and once after boot — covering the cases where the
 * app isn't opened: a new calendar day, or a device reboot that cleared pending AlarmManager alarms.
 */
class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val timeProvider: TimeProvider by inject()
    private val calculateDailyGoal: CalculateDailyGoalUseCase by inject()
    private val rescheduleReminders: RescheduleRemindersUseCase by inject()

    override suspend fun doWork(): Result = try {
        // Ensures a CALCULATED goal exists for today (no-op/respected if MANUAL or no profile yet).
        calculateDailyGoal(timeProvider.today())
        rescheduleReminders()
        Result.success()
    } catch (t: Throwable) {
        Result.retry()
    }
}
