package com.bose.hydrohabit.core.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** Combine a [LocalDate] + [LocalTime] into an [Instant] in the given [zone]. */
fun LocalDate.atTime(time: LocalTime, zone: TimeZone): Instant =
    LocalDateTime(this, time).toInstant(zone)

/** The [LocalDate] this instant falls on in [zone]. */
fun Instant.toLocalDate(zone: TimeZone): LocalDate = toLocalDateTime(zone).date

/** The [LocalTime] this instant falls on in [zone]. */
fun Instant.toLocalTime(zone: TimeZone): LocalTime = toLocalDateTime(zone).time

/** Inclusive list of dates from [this] to [end]. */
fun LocalDate.datesUntilInclusive(end: LocalDate): List<LocalDate> {
    val days = this.daysUntil(end)
    if (days < 0) return emptyList()
    return (0..days).map { this.plus(it, DateTimeUnit.DAY) }
}

fun LocalDate.startOfDay(zone: TimeZone): Instant = atStartOfDayIn(zone)

/**
 * True when [sleep] is on the calendar day after [wake] (i.e. the awake window crosses midnight),
 * e.g. wake 07:00, sleep 01:00.
 */
fun crossesMidnight(wake: LocalTime, sleep: LocalTime): Boolean = sleep <= wake
