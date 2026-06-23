package com.bose.hydrohabit.domain.model

import kotlinx.datetime.LocalTime

/**
 * User-configurable behavior of the reminder engine.
 *
 * [wakeTime]/[sleepTime] default from the profile but can diverge (e.g. quieter reminder window
 * than the awake window). [strategy] selects the scheduling algorithm in `ReminderScheduler`.
 */
data class ReminderSettings(
    val enabled: Boolean = true,
    val intervalMinutes: Int = 90,
    val wakeTime: LocalTime,
    val sleepTime: LocalTime,
    val strategy: ReminderStrategy = ReminderStrategy.ADAPTIVE,
    val quietWindows: List<TimeRange> = emptyList(),
    /** Suppress a reminder if water was logged within this many minutes before it. */
    val skipIfRecentlyLoggedMinutes: Int = 30,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
) {
    companion object {
        /** Sensible defaults used until the user has a profile / saved settings. */
        val DEFAULT = ReminderSettings(
            wakeTime = LocalTime(7, 0),
            sleepTime = LocalTime(23, 0),
        )
    }
}

enum class ReminderStrategy {
    /** Evenly spaced every [ReminderSettings.intervalMinutes]. */
    FIXED_INTERVAL,

    /** Spacing adapts to remaining goal, time left in the day, and recent activity. */
    ADAPTIVE,
}

/** A wall-clock time window [start, end). [end] before [start] means it wraps past midnight. */
data class TimeRange(val start: LocalTime, val end: LocalTime) {
    fun contains(time: LocalTime): Boolean =
        if (start <= end) time >= start && time < end
        else time >= start || time < end
}
