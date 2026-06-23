package com.bose.hydrohabit.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime

/**
 * The person using the app. Drives personalized hydration goals and the reminder window.
 *
 * [gender] is optional (nullable) per the requirements. Times are wall-clock [LocalTime] in the
 * user's local zone; goal math and reminder scheduling resolve them against a [TimeProvider].
 */
data class UserProfile(
    val id: String,
    val weightKg: Double,
    val age: Int,
    val gender: Gender?,
    val activityLevel: ActivityLevel,
    val wakeTime: LocalTime,
    val sleepTime: LocalTime,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class Gender { MALE, FEMALE, OTHER, UNSPECIFIED }

/** Activity level with the multiplier applied to the base hydration goal. */
enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(1.0),
    LIGHT(1.1),
    MODERATE(1.2),
    ACTIVE(1.375),
    VERY_ACTIVE(1.55),
}

enum class UnitSystem { METRIC, IMPERIAL }
