package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.core.time.atTime
import com.bose.hydrohabit.domain.model.DailyProgress
import com.bose.hydrohabit.domain.model.ReminderReason
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.ReminderStrategy
import com.bose.hydrohabit.domain.model.TimeRange
import com.bose.hydrohabit.testutil.SequentialIdGenerator
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReminderSchedulerTest {

    private val zone = TimeZone.UTC
    private val today = LocalDate(2026, 6, 18)
    private fun at(h: Int, m: Int = 0): Instant = today.atTime(LocalTime(h, m), zone)

    private fun scheduler() = ReminderScheduler(SequentialIdGenerator("rem"))

    private fun settings(
        enabled: Boolean = true,
        interval: Int = 120,
        strategy: ReminderStrategy = ReminderStrategy.FIXED_INTERVAL,
        quiet: List<TimeRange> = emptyList(),
        skipMinutes: Int = 30,
    ) = ReminderSettings(
        enabled = enabled,
        intervalMinutes = interval,
        wakeTime = LocalTime(7, 0),
        sleepTime = LocalTime(23, 0),
        strategy = strategy,
        quietWindows = quiet,
        skipIfRecentlyLoggedMinutes = skipMinutes,
    )

    private fun progress(consumed: Int, goal: Int = 2000) =
        DailyProgress(date = today, goalMl = goal, consumedMl = consumed, entryCount = 0, lastEntryAt = null)

    @Test
    fun disabledProducesNoReminders() {
        val plan = scheduler().plan(settings(enabled = false), progress(0), at(8), zone, null)
        assertTrue(plan.isEmpty())
    }

    @Test
    fun metGoalProducesNoReminders() {
        val plan = scheduler().plan(settings(), progress(consumed = 2000), at(8), zone, null)
        assertTrue(plan.isEmpty())
    }

    @Test
    fun afterSleepProducesNoReminders() {
        val plan = scheduler().plan(settings(), progress(500), at(23, 30), zone, null)
        assertTrue(plan.isEmpty())
    }

    @Test
    fun fixedIntervalSpreadsAcrossAwakeWindow() {
        val plan = scheduler().plan(settings(interval = 120), progress(0), at(7), zone, null)
        // 07:00 → 23:00 step 2h = 9 slots
        assertEquals(9, plan.size)
        assertEquals(at(7), plan.first().scheduledAt)
        assertEquals(at(23), plan.last().scheduledAt)
        assertEquals(ReminderReason.ROUTINE, plan.first().reason)
    }

    @Test
    fun remainingGoalIsDistributedAcrossReminders() {
        val plan = scheduler().plan(settings(interval = 120), progress(0, goal = 2000), at(7), zone, null)
        // ceil(2000 / 9) = 223
        assertEquals(223, plan.first().expectedAmountMl)
    }

    @Test
    fun recentLogPushesFirstReminderPastCooldown() {
        val plan = scheduler().plan(
            settings(interval = 120, skipMinutes = 30),
            progress(0),
            now = at(7),
            zone = zone,
            lastEntryAt = at(6, 50),
        )
        // cooldown ends 06:50 + 30m = 07:20, which becomes the first slot
        assertEquals(at(7, 20), plan.first().scheduledAt)
    }

    @Test
    fun quietWindowsAreSkipped() {
        val plan = scheduler().plan(
            settings(interval = 60, quiet = listOf(TimeRange(LocalTime(12, 0), LocalTime(14, 0)))),
            progress(0),
            now = at(7),
            zone = zone,
            lastEntryAt = null,
        )
        val times = plan.map { it.scheduledAt }
        assertTrue(at(12) !in times)
        assertTrue(at(13) !in times)
        assertTrue(at(14) in times)
    }

    @Test
    fun behindPaceFlagsFirstReminderAndTightensInterval() {
        val behindPlan = scheduler().plan(
            settings(interval = 120, strategy = ReminderStrategy.ADAPTIVE),
            progress(consumed = 200, goal = 2000), // far below the ~1000ml expected by midday
            now = at(15),
            zone = zone,
            lastEntryAt = at(14),
        )
        assertEquals(ReminderReason.BEHIND_GOAL, behindPlan.first().reason)

        val onPacePlan = scheduler().plan(
            settings(interval = 120, strategy = ReminderStrategy.ADAPTIVE),
            progress(consumed = 1000, goal = 2000),
            now = at(15),
            zone = zone,
            lastEntryAt = at(14, 50),
        )
        // Behind ⇒ tighter spacing ⇒ at least as many reminders as the on-pace plan.
        assertTrue(behindPlan.size >= onPacePlan.size)
    }

    @Test
    fun catchUpReasonWhenNothingLoggedWellIntoDay() {
        val plan = scheduler().plan(
            settings(interval = 120, strategy = ReminderStrategy.ADAPTIVE),
            progress(consumed = 0, goal = 2000),
            now = at(10), // 3h after wake, no logs
            zone = zone,
            lastEntryAt = null,
        )
        assertEquals(ReminderReason.BEHIND_GOAL, plan.first().reason)
    }
}
