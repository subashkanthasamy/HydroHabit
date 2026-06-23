package com.bose.hydrohabit.domain.reminders

import com.bose.hydrohabit.domain.model.ScheduledReminder

/**
 * Port for OS-level reminder scheduling. The pure `ReminderScheduler` decides the plan; this
 * executes it. Implemented per platform (Android WorkManager/AlarmManager, iOS UNUserNotification)
 * and injected via Koin — the domain never references platform APIs.
 */
interface PlatformReminderScheduler {
    /** Replace any previously scheduled reminders with [reminders]. */
    suspend fun schedule(reminders: List<ScheduledReminder>)

    /** Cancel all pending reminders (e.g. when the user disables them). */
    suspend fun cancelAll()
}

/** Port for requesting/checking notification permission (Android 13+, iOS authorization). */
interface NotificationPermissionController {
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
}
