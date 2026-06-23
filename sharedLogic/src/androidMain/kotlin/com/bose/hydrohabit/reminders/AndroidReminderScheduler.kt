package com.bose.hydrohabit.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bose.hydrohabit.domain.model.ReminderReason
import com.bose.hydrohabit.domain.model.ScheduledReminder
import com.bose.hydrohabit.domain.reminders.PlatformReminderScheduler

/**
 * Executes a reminder plan with [AlarmManager], firing [ReminderBroadcastReceiver] at each time.
 * Uses inexact alarms (`set`) to avoid the exact-alarm permission; precise timing isn't critical
 * for hydration nudges.
 */
class AndroidReminderScheduler(private val context: Context) : PlatformReminderScheduler {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override suspend fun schedule(reminders: List<ScheduledReminder>) {
        cancelAll()
        reminders.forEachIndexed { index, reminder ->
            val requestCode = BASE_REQUEST_CODE + index
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intentFor(reminder, requestCode),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            scheduledCodes.add(requestCode)
            // setAndAllowWhileIdle fires even in Doze and needs no exact-alarm permission; exact
            // timing isn't critical for hydration nudges.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.scheduledAt.toEpochMilliseconds(),
                pendingIntent,
            )
        }
    }

    override suspend fun cancelAll() {
        scheduledCodes.toList().forEach { code ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                Intent(context, ReminderBroadcastReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
        scheduledCodes.clear()
    }

    private fun intentFor(reminder: ScheduledReminder, requestCode: Int) =
        Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_ID, requestCode)
            putExtra(ReminderBroadcastReceiver.EXTRA_TITLE, titleFor(reminder.reason))
            putExtra(ReminderBroadcastReceiver.EXTRA_TEXT, "Time to drink about ${reminder.expectedAmountMl} ml")
        }

    private fun titleFor(reason: ReminderReason) = when (reason) {
        ReminderReason.ROUTINE -> "Time to hydrate"
        ReminderReason.BEHIND_GOAL -> "You're a bit behind — hydrate!"
        ReminderReason.CATCH_UP_MISSED -> "Don't forget to drink water"
    }

    companion object {
        private const val BASE_REQUEST_CODE = 10_000
        // Tracks scheduled request codes so cancelAll can target them. Process-lifetime cache; a
        // production build would also persist these to survive restarts.
        private val scheduledCodes = mutableListOf<Int>()
    }
}
