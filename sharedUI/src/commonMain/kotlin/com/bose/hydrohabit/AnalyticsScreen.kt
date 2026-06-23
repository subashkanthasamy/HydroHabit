package com.bose.hydrohabit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.domain.model.AnalyticsReport
import com.bose.hydrohabit.domain.model.ReportPeriod
import com.bose.hydrohabit.presentation.analytics.AnalyticsState

@Composable
fun AnalyticsScreen(
    state: AnalyticsState,
    onSelectPeriod: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportPeriod.entries.forEach { period ->
                FilterChip(
                    selected = state.period == period,
                    onClick = { onSelectPeriod(period) },
                    label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }

        val report = state.report
        if (report == null || report.dailyBreakdown.isEmpty()) {
            Text("Log some water to see your trends.", style = MaterialTheme.typography.bodyMedium)
        } else {
            SummaryCard(report)
            BreakdownCard(report)
            report.insights.forEach { insight ->
                Card(Modifier.fillMaxWidth()) {
                    Text(insight.message, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(report: AnalyticsReport) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Average: ${report.averageMl} ml/day", fontWeight = FontWeight.Bold)
            Text("Goal completion: ${(report.goalCompletionRate * 100).toInt()}%")
            report.bestDay?.let { Text("Best day: ${it.date} (${it.consumedMl} ml)") }
            report.worstDay?.let { Text("Lowest day: ${it.date} (${it.consumedMl} ml)") }
        }
    }
}

@Composable
private fun BreakdownCard(report: AnalyticsReport) {
    val max = report.dailyBreakdown.maxOf { it.consumedMl }.coerceAtLeast(1)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Daily breakdown", fontWeight = FontWeight.Bold)
            report.dailyBreakdown.forEach { day ->
                Column {
                    Text("${day.date} — ${day.consumedMl} ml", style = MaterialTheme.typography.bodySmall)
                    Box(
                        Modifier
                            .fillMaxWidth(day.consumedMl.toFloat() / max)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}
