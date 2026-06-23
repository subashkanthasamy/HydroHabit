package com.bose.hydrohabit.domain.model

import kotlinx.datetime.Instant

/**
 * A milestone the user can unlock. The full catalog is seeded from [AchievementId]; per-user
 * state ([unlockedAt], [progress]) is persisted and updated by `AchievementEvaluator`.
 */
data class Achievement(
    val id: AchievementId,
    val title: String,
    val description: String,
    val type: AchievementType,
    /** Numeric target this achievement is measured against (days, liters, count…). */
    val threshold: Double,
    val unlockedAt: Instant? = null,
    /** 0f..1f progress toward [threshold]. */
    val progress: Float = 0f,
) {
    val isUnlocked: Boolean get() = unlockedAt != null
}

/** What kind of metric an achievement tracks — drives which rule evaluates it. */
enum class AchievementType {
    FIRST_ACTION,
    DAILY_STREAK,
    TOTAL_VOLUME,
    PERFECT_PERIOD,
    COMPOSITE,
}

/**
 * Stable identifiers for the seeded catalog. Adding a milestone = add an id here plus a rule in
 * `AchievementEvaluator` — no schema change required (data-driven, per the roadmap).
 */
enum class AchievementId {
    FIRST_LOG,
    STREAK_7,
    STREAK_30,
    LITERS_100,
    PERFECT_WEEK,
    HYDRATION_MASTER,
}
