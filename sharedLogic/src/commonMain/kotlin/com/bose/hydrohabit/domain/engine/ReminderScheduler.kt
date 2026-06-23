package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.core.time.atTime
import com.bose.hydrohabit.core.time.crossesMidnight
import com.bose.hydrohabit.core.time.toLocalDate
import com.bose.hydrohabit.core.time.toLocalTime
import com.bose.hydrohabit.core.util.IdGenerator
import com.bose.hydrohabit.domain.model.DailyProgress
import com.bose.hydrohabit.domain.model.ReminderReason
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.ReminderStrategy
import com.bose.hydrohabit.domain.model.ScheduledReminder
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.math.ceil
import kotlin.time.Duration.Companion.minutes

/**
 * Pure engine that decides *what* reminders should fire today. It performs no scheduling itself —
 * the platform layer executes the returned plan. Being pure makes every adaptive rule unit-testable.
 *
 * Rules:
 *  - Only schedules within the awake window [wakeTime, sleepTime] (handles past-midnight sleep).
 *  - Stops once the daily goal is met (nothing remaining → no reminders).
 *  - Skips the immediate reminder if water was logged within `skipIfRecentlyLoggedMinutes`.
 *  - ADAPTIVE: tightens spacing and flags BEHIND_GOAL when the user trails the expected pace;
 *    flags CATCH_UP_MISSED when nothing has been logged well into the day.
 *  - Honors quiet windows.
 *  - Distributes the remaining goal evenly across the planned reminders (`expectedAmountMl`).
 */
class ReminderScheduler(
    private val idGenerator: IdGenerator,
    private val config: ReminderSchedulerConfig = ReminderSchedulerConfig(),
) {
    fun plan(
        settings: ReminderSettings,
        progress: DailyProgress,
        now: Instant,
        zone: TimeZone,
        lastEntryAt: Instant?,
    ): List<ScheduledReminder> {
        if (!settings.enabled || progress.remainingMl <= 0) return emptyList()

        val today = now.toLocalDate(zone)
        val wakeInstant = today.atTime(settings.wakeTime, zone)
        val sleepInstant = today.atTime(settings.sleepTime, zone).let {
            if (crossesMidnight(settings.wakeTime, settings.sleepTime)) it.plus(1, DateTimeUnit.DAY, zone) else it
        }
        if (now >= sleepInstant) return emptyList()

        // First candidate slot: not in the past, not before wake, and after any recent-log cooldown.
        var cursor = maxOf(now, wakeInstant)
        if (lastEntryAt != null) {
            val cooldownEnd = lastEntryAt + settings.skipIfRecentlyLoggedMinutes.minutes
            if (cooldownEnd > cursor) cursor = cooldownEnd
        }
        if (cursor >= sleepInstant) return emptyList()

        val behind = isBehindPace(progress, now, wakeInstant, sleepInstant)
        val missed = lastEntryAt == null && now >= wakeInstant + config.missedGraceMinutes.minutes

        val interval = when (settings.strategy) {
            ReminderStrategy.FIXED_INTERVAL -> settings.intervalMinutes
            ReminderStrategy.ADAPTIVE ->
                if (behind) maxOf(config.minIntervalMinutes, (settings.intervalMinutes * config.behindTightenFactor).toInt())
                else settings.intervalMinutes
        }.coerceAtLeast(config.minIntervalMinutes)

        val slots = buildList {
            var t = cursor
            while (t <= sleepInstant && size < config.maxRemindersPerDay) {
                if (!inQuietWindow(t, settings, zone)) add(t)
                t += interval.minutes
            }
        }
        if (slots.isEmpty()) return emptyList()

        val perReminder = ceil(progress.remainingMl.toDouble() / slots.size).toInt()
        return slots.mapIndexed { index, at ->
            val reason = when {
                index == 0 && behind -> ReminderReason.BEHIND_GOAL
                index == 0 && missed -> ReminderReason.CATCH_UP_MISSED
                else -> ReminderReason.ROUTINE
            }
            ScheduledReminder(
                id = idGenerator.newId(),
                scheduledAt = at,
                expectedAmountMl = perReminder,
                reason = reason,
            )
        }
    }

    /** True when consumption trails the time-proportional expectation (with a tolerance band). */
    private fun isBehindPace(
        progress: DailyProgress,
        now: Instant,
        wake: Instant,
        sleep: Instant,
    ): Boolean {
        val total = sleep - wake
        val elapsed = now - wake
        if (elapsed <= kotlin.time.Duration.ZERO || total <= kotlin.time.Duration.ZERO) return false
        val expectedFraction = (elapsed / total).coerceIn(0.0, 1.0)
        val expectedMl = progress.goalMl * expectedFraction
        return progress.consumedMl < expectedMl * config.behindToleranceFactor
    }

    private fun inQuietWindow(at: Instant, settings: ReminderSettings, zone: TimeZone): Boolean {
        if (settings.quietWindows.isEmpty()) return false
        val localTime = at.toLocalTime(zone)
        return settings.quietWindows.any { it.contains(localTime) }
    }
}

data class ReminderSchedulerConfig(
    val minIntervalMinutes: Int = 30,
    val maxRemindersPerDay: Int = 24,
    /** Multiplier applied to the interval when the user is behind pace (tighter spacing). */
    val behindTightenFactor: Double = 0.66,
    /** Consumed must be below expected * this factor to count as "behind". */
    val behindToleranceFactor: Double = 0.9,
    /** No log by wake + this many minutes ⇒ first reminder is a catch-up. */
    val missedGraceMinutes: Int = 120,
)
