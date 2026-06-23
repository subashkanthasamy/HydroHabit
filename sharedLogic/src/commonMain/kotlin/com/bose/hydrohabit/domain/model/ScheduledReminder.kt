package com.bose.hydrohabit.domain.model

import kotlinx.datetime.Instant

/**
 * One reminder the engine wants the OS to fire. Produced by `ReminderScheduler` (pure) and handed
 * to the platform scheduler for execution. Not persisted as domain state — recomputed each plan.
 */
data class ScheduledReminder(
    val id: String,
    val scheduledAt: Instant,
    /** How much the engine suggests drinking at this point to stay on track. */
    val expectedAmountMl: Int,
    val reason: ReminderReason,
)

enum class ReminderReason {
    /** Regular spaced reminder. */
    ROUTINE,

    /** Issued because the user is behind the projected pace. */
    BEHIND_GOAL,

    /** Compensating for a previously missed/ignored reminder. */
    CATCH_UP_MISSED,
}
