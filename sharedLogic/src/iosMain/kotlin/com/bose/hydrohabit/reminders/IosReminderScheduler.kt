package com.bose.hydrohabit.reminders

import com.bose.hydrohabit.domain.model.ReminderReason
import com.bose.hydrohabit.domain.model.ScheduledReminder
import com.bose.hydrohabit.domain.reminders.PlatformReminderScheduler
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * Schedules iOS local notifications. Unlike Android, iOS displays scheduled local notifications
 * itself, so no background receiver is required.
 */
class IosReminderScheduler : PlatformReminderScheduler {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun schedule(reminders: List<ScheduledReminder>) {
        cancelAll()
        val nowSeconds = NSDate().timeIntervalSince1970
        reminders.forEach { reminder ->
            val fireSeconds = reminder.scheduledAt.toEpochMilliseconds() / 1000.0
            val interval = fireSeconds - nowSeconds
            if (interval <= 0) return@forEach

            val content = UNMutableNotificationContent().apply {
                setTitle(titleFor(reminder.reason))
                setBody("Time to drink about ${reminder.expectedAmountMl} ml")
                setSound(UNNotificationSound.defaultSound)
            }
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(interval, repeats = false)
            val request = UNNotificationRequest.requestWithIdentifier(reminder.id, content, trigger)
            center.addNotificationRequest(request, withCompletionHandler = null)
        }
    }

    override suspend fun cancelAll() {
        center.removeAllPendingNotificationRequests()
    }

    private fun titleFor(reason: ReminderReason) = when (reason) {
        ReminderReason.ROUTINE -> "Time to hydrate"
        ReminderReason.BEHIND_GOAL -> "You're a bit behind — hydrate!"
        ReminderReason.CATCH_UP_MISSED -> "Don't forget to drink water"
    }
}
