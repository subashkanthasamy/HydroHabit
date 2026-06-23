package com.bose.hydrohabit.core.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Abstraction over the system clock and timezone.
 *
 * Engines and use cases depend on this rather than [Clock.System] directly so that time is
 * injectable and tests are fully deterministic (see `FixedTimeProvider` in tests).
 */
interface TimeProvider {
    fun now(): Instant
    fun timeZone(): TimeZone

    fun today(): LocalDate = now().toLocalDateTime(timeZone()).date
    fun nowDateTime(): LocalDateTime = now().toLocalDateTime(timeZone())
}

/** Production implementation backed by the real device clock. */
class SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Clock.System.now()
    override fun timeZone(): TimeZone = TimeZone.currentSystemDefault()
}
