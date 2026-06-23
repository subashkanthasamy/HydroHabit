package com.bose.hydrohabit

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
            fontWeight = FontWeight.Bold,
        )

        // Period selector — SegmentedControl maps index → ReportPeriod enum value
        SegmentedControl(
            options = PERIOD_LABELS,
            selectedIndex = PERIOD_VALUES.indexOf(state.period).coerceAtLeast(0),
            onSelect = { idx -> onSelectPeriod(PERIOD_VALUES[idx]) },
        )

        val report = state.report
        if (report == null || report.dailyBreakdown.isEmpty()) {
            EmptyState()
        } else {
            SummaryCard(report)
            WeeklyCompletionCard(report)
            BreakdownChartCard(report)
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
            Text(
                "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
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
            fontWeight = FontWeight.SemiBold,
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
            Text(
                "Daily Completion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            report.dailyBreakdown.forEach { day ->
                val met = day.metGoal()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Check circle
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
                                fontWeight = FontWeight.Bold,
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
                        fontWeight = FontWeight.SemiBold,
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
private fun BreakdownChartCard(report: AnalyticsReport) {
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
                Text(
                    "Daily Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
                    Text(
                        "${report.range.start} – ${report.range.end}",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurface,
                    )
                }
            }
            BarChart(bars = bars, height = 120.dp)
        }
    }
}
