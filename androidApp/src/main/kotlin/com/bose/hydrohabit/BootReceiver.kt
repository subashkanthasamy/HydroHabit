package com.bose.hydrohabit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bose.hydrohabit.work.ReminderWorkScheduler

/**
 * Re-arms reminders after a reboot. AlarmManager drops all pending alarms on reboot, so we both
 * re-enqueue the daily periodic work and run a one-off recompute+reschedule immediately.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val appContext = context.applicationContext
            ReminderWorkScheduler.ensureScheduled(appContext)
            ReminderWorkScheduler.runNow(appContext)
        }
    }
}
