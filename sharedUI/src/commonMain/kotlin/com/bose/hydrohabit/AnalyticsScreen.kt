package com.bose.hydrohabit

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.components.BarChart
import com.bose.hydrohabit.components.BarDatum
import com.bose.hydrohabit.components.SegmentedControl
import com.bose.hydrohabit.domain.model.AnalyticsReport
import com.bose.hydrohabit.domain.model.DaySummary
import com.bose.hydrohabit.domain.model.ReportPeriod
import com.bose.hydrohabit.presentation.analytics.AnalyticsState
import com.bose.hydrohabit.theme.glassCard

// ReportPeriod enum order: DAILY(0), WEEKLY(1), MONTHLY(2)
private val PERIOD_LABELS = listOf("Day", "Week", "Month")
private val PERIOD_VALUES = listOf(ReportPeriod.DAILY, ReportPeriod.WEEKLY, ReportPeriod.MONTHLY)

// Minimum bar width for the scrollable monthly chart so bars remain legible
private val MIN_BAR_WIDTH = 24.dp

@Composable
fun AnalyticsScreen(
    state: AnalyticsState,
    onSelectPeriod: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Insights",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Period selector — SegmentedControl maps index → ReportPeriod enum value
        SegmentedControl(
            options = PERIOD_LABELS,
            selectedIndex = PERIOD_VALUES.indexOf(state.period).coerceAtLeast(0),
            onSelect = { idx -> onSelectPeriod(PERIOD_VALUES[idx]) },
        )

        val report = state.report
        when {
            // A3: distinct loading branch — fires while isLoading=true (report may still be null)
            state.isLoading -> {
                LoadingState()
            }
            report == null || report.dailyBreakdown.isEmpty() -> {
                EmptyState()
            }
            else -> {
                SummaryCard(report)
                WeeklyCompletionCard(report)
                BreakdownChartCard(report, state.period)
                report.insights.forEach { insight ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .glassCard(shape = RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            insight.message,
                            Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(16.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = "Loading" },
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(16.dp))
    ) {
        Text(
            "Log some water to see your trends.",
            Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary Card — avg, goal completion %, best/worst day
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(report: AnalyticsReport) {
    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // A4: drop redundant fontWeight on titleMedium
            Text(
                "Summary",
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile(
                    label = "Avg / day",
                    value = "${report.averageMl} ml",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "Goal met",
                    value = "${(report.goalCompletionRate * 100).toInt()}%",
                    modifier = Modifier.weight(1f),
                )
            }
            report.bestDay?.let { best ->
                DayTile(label = "Best day", day = best)
            }
            report.worstDay?.let { worst ->
                DayTile(label = "Lowest day", day = worst)
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .background(color = scheme.secondaryContainer, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // A5: use onSecondaryContainer on secondaryContainer background
            // A4: drop redundant fontWeight on titleMedium
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSecondaryContainer,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSecondaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun DayTile(label: String, day: DaySummary) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = scheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        Text(
            "${day.date}  ·  ${day.consumedMl} ml",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurface,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Weekly Completion Card — check row for each day in breakdown
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WeeklyCompletionCard(report: AnalyticsReport) {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // A4: drop redundant fontWeight on titleMedium
            Text(
                "Daily Completion",
                style = MaterialTheme.typography.titleMedium,
            )
            report.dailyBreakdown.forEach { day ->
                val met = day.metGoal()
                // A2: row-level merged contentDescription so screen readers convey goal state
                val rowDesc = "${day.date}, ${day.consumedMl} ml, ${if (met) "goal met" else "goal not met"}"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) { contentDescription = rowDesc },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Check circle — visual only; semantics on the Row covers the state
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (met) scheme.primary else scheme.surfaceVariant,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (met) {
                            Text(
                                "✓",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onPrimary,
                            )
                        }
                    }
                    Text(
                        "${day.date}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        color = scheme.onSurface,
                    )
                    Text(
                        "${day.consumedMl} ml",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (met) scheme.primary else scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Breakdown Chart Card — BarChart from real dailyBreakdown series
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BreakdownChartCard(report: AnalyticsReport, period: ReportPeriod) {
    // Build BarDatum list from the real dailyBreakdown.
    // fraction = consumedMl / goalMl if goalMl > 0, else consumedMl / max consumed in period.
    // Highlighted = day that met the goal.
    val maxConsumed = report.dailyBreakdown.maxOf { it.consumedMl }.coerceAtLeast(1)
    val bars: List<BarDatum> = report.dailyBreakdown.map { day ->
        val fraction = when {
            day.goalMl > 0 -> (day.consumedMl.toFloat() / day.goalMl).coerceIn(0f, 1f)
            else -> (day.consumedMl.toFloat() / maxConsumed).coerceIn(0f, 1f)
        }
        // Label: use day-of-month abbreviation (e.g. "12") for clarity across all period types
        val label = day.date.dayOfMonth.toString()
        BarDatum(label = label, fraction = fraction, highlighted = day.metGoal())
    }

    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // A4: drop redundant fontWeight on titleMedium
                Text(
                    "Daily Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                )
                val scheme = MaterialTheme.colorScheme
                Box(
                    modifier = Modifier
                        .background(
                            color = scheme.secondaryContainer,
                            shape = RoundedCornerShape(50),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    // A5: use onSecondaryContainer on secondaryContainer background
                    Text(
                        "${report.range.start} – ${report.range.end}",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSecondaryContainer,
                    )
                }
            }

            // A6: for long periods (MONTHLY ~30 bars) wrap in a horizontally scrollable row
            // so each bar has a sensible minimum width and labels don't collide.
            // Short periods (DAILY/WEEKLY ≤ 7 bars) fit within the card width — no scroll needed.
            val barCount = bars.size
            val useScroll = period == ReportPeriod.MONTHLY || barCount > 10
            if (useScroll) {
                val minChartWidth = (MIN_BAR_WIDTH + 8.dp) * barCount.coerceAtLeast(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    BarChart(
                        bars = bars,
                        height = 120.dp,
                        modifier = Modifier.widthIn(min = minChartWidth),
                    )
                }
            } else {
                BarChart(bars = bars, height = 120.dp)
            }
        }
    }
}
