package com.bose.hydrohabit.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * A single logged consumption of water.
 *
 * [date] is denormalized from [timestamp] (in the user's zone at log time) so daily queries and
 * indexes are cheap and stable across timezone changes.
 */
data class WaterEntry(
    val id: String,
    val amountMl: Int,
    val timestamp: Instant,
    val date: LocalDate,
    val source: EntrySource = EntrySource.CUSTOM,
    val note: String? = null,
)

enum class EntrySource {
    QUICK_ADD,
    CUSTOM,
    /** Logged in response to a reminder. */
    REMINDER,
    /** Imported from Health Connect / HealthKit / a smart bottle (Phase 3 & 5). */
    IMPORTED,
}
