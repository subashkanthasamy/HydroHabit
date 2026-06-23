package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.model.Achievement
import com.bose.hydrohabit.domain.model.AchievementId
import com.bose.hydrohabit.domain.model.AchievementType

/**
 * Aggregated lifetime stats the achievement rules are measured against. Assembled by the data
 * layer from the full history; keeping it a flat value object keeps the evaluator pure.
 */
data class LifetimeStats(
    val totalEntries: Int,
    val totalVolumeMl: Long,
    val currentDailyStreak: Int,
    val longestDailyStreak: Int,
    val weeklyStreak: Int,
    val goalCompletionRate: Float,
)

data class AchievementEvaluation(
    val updated: List<Achievement>,
    val newlyUnlocked: List<Achievement>,
)

/**
 * Evaluates the achievement catalog against [LifetimeStats]. Each milestone is a declarative
 * [Rule] in [rules] — adding a new achievement means adding an id + a rule, no control-flow edits.
 * Uses the longest streak ever for streak milestones so they stay unlocked after a streak resets.
 */
class AchievementEvaluator(
    private val timeProvider: TimeProvider,
) {
    private data class Rule(
        val type: AchievementType,
        val threshold: Double,
        val title: String,
        val description: String,
        val measure: (LifetimeStats) -> Double,
    )

    private val rules: Map<AchievementId, Rule> = mapOf(
        AchievementId.FIRST_LOG to Rule(
            AchievementType.FIRST_ACTION, 1.0, "First Sip", "Log your first water entry",
        ) { it.totalEntries.toDouble() },
        AchievementId.STREAK_7 to Rule(
            AchievementType.DAILY_STREAK, 7.0, "7-Day Streak", "Hit your goal 7 days in a row",
        ) { it.longestDailyStreak.toDouble() },
        AchievementId.STREAK_30 to Rule(
            AchievementType.DAILY_STREAK, 30.0, "30-Day Streak", "Hit your goal 30 days in a row",
        ) { it.longestDailyStreak.toDouble() },
        AchievementId.LITERS_100 to Rule(
            AchievementType.TOTAL_VOLUME, 100.0, "Century Club", "Drink 100 liters in total",
        ) { it.totalVolumeMl / 1000.0 },
        AchievementId.PERFECT_WEEK to Rule(
            AchievementType.PERFECT_PERIOD, 1.0, "Perfect Week", "Complete a full qualifying week",
        ) { it.weeklyStreak.toDouble() },
        AchievementId.HYDRATION_MASTER to Rule(
            AchievementType.COMPOSITE, 3.0, "Hydration Master",
            "30-day streak, 100L consumed, and 90%+ goal completion",
        ) { stats ->
            // Composite: counts how many of the three sub-goals are met (0..3).
            var met = 0.0
            if (stats.longestDailyStreak >= 30) met++
            if (stats.totalVolumeMl >= 100_000) met++
            if (stats.goalCompletionRate >= 0.9f) met++
            met
        },
    )

    /** The full locked catalog, for first-run seeding. */
    fun seedCatalog(): List<Achievement> = rules.map { (id, rule) ->
        Achievement(
            id = id,
            title = rule.title,
            description = rule.description,
            type = rule.type,
            threshold = rule.threshold,
            unlockedAt = null,
            progress = 0f,
        )
    }

    /** Recompute progress/unlock state. Already-unlocked achievements are never re-locked. */
    fun evaluate(current: List<Achievement>, stats: LifetimeStats): AchievementEvaluation {
        val byId = current.associateBy { it.id }
        val newlyUnlocked = mutableListOf<Achievement>()

        val updated = rules.map { (id, rule) ->
            val existing = byId[id] ?: seedFor(id, rule)
            if (existing.isUnlocked) return@map existing

            val value = rule.measure(stats)
            val progress = (value / rule.threshold).coerceIn(0.0, 1.0).toFloat()
            val unlock = value >= rule.threshold

            existing.copy(
                progress = progress,
                unlockedAt = if (unlock) timeProvider.now() else null,
            ).also { if (unlock) newlyUnlocked += it }
        }
        return AchievementEvaluation(updated = updated, newlyUnlocked = newlyUnlocked)
    }

    private fun seedFor(id: AchievementId, rule: Rule) = Achievement(
        id = id, title = rule.title, description = rule.description,
        type = rule.type, threshold = rule.threshold,
    )
}
