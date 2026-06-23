package com.bose.hydrohabit.testutil

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.core.util.IdGenerator
import com.bose.hydrohabit.domain.model.ActivityLevel
import com.bose.hydrohabit.domain.model.DaySummary
import com.bose.hydrohabit.domain.model.Gender
import com.bose.hydrohabit.domain.model.UnitSystem
import com.bose.hydrohabit.domain.model.UserProfile
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/** A [TimeProvider] frozen at a fixed instant — makes time-dependent logic deterministic. */
class FixedTimeProvider(
    private var instant: Instant,
    private val zone: TimeZone = TimeZone.UTC,
) : TimeProvider {
    override fun now(): Instant = instant
    override fun timeZone(): TimeZone = zone
    fun advanceTo(newInstant: Instant) { instant = newInstant }
}

/** Deterministic, sequential id generator: "id-0", "id-1", … */
class SequentialIdGenerator(prefix: String = "id") : IdGenerator {
    private var counter = 0
    private val prefix = prefix
    override fun newId(): String = "$prefix-${counter++}"
}

fun sampleProfile(
    id: String = "user-1",
    weightKg: Double = 70.0,
    age: Int = 30,
    gender: Gender? = Gender.MALE,
    activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    wakeTime: LocalTime = LocalTime(7, 0),
    sleepTime: LocalTime = LocalTime(23, 0),
    unitSystem: UnitSystem = UnitSystem.METRIC,
    createdAt: Instant = Instant.fromEpochMilliseconds(0),
    updatedAt: Instant = Instant.fromEpochMilliseconds(0),
) = UserProfile(
    id = id,
    weightKg = weightKg,
    age = age,
    gender = gender,
    activityLevel = activityLevel,
    wakeTime = wakeTime,
    sleepTime = sleepTime,
    unitSystem = unitSystem,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun daySummary(
    date: LocalDate,
    consumedMl: Int,
    goalMl: Int = 2000,
    entryCount: Int = 4,
) = DaySummary(date = date, consumedMl = consumedMl, goalMl = goalMl, entryCount = entryCount)
