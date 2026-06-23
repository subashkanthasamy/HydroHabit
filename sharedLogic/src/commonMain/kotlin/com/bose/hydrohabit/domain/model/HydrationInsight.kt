package com.bose.hydrohabit.domain.model

import kotlinx.datetime.Instant

/**
 * A human-readable, personalized observation produced by `AnalyticsCalculator`. Derived on demand
 * (never stored) so insights always reflect current data.
 */
data class HydrationInsight(
    val id: String,
    val type: InsightType,
    val message: String,
    /** Optional supporting figure (e.g. percent change) the UI can render as a badge. */
    val metricValue: Double? = null,
    val generatedAt: Instant,
)

enum class InsightType {
    POSITIVE,
    WARNING,
    TIP,
    TREND,
}
