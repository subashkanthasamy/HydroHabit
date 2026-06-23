package com.bose.hydrohabit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.domain.model.EntrySource
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.presentation.history.HistoryState
import com.bose.hydrohabit.theme.glassCard
import com.bose.hydrohabit.theme.softCard
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HistoryScreen(
    state: HistoryState,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Date navigation header ──────────────────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .softCard(shape = RoundedCornerShape(22.dp), elevation = 6.dp)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Previous day — soft circular button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .softCard(shape = CircleShape, elevation = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    TextButton(
                        onClick = onPreviousDay,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Text(
                            "‹",
                            style = MaterialTheme.typography.titleLarge,
                            color = scheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                // Date + total
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.date?.toString() ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                    Text(
                        text = "${state.dailyTotalMl} ml total",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }

                // Next day — soft circular button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .softCard(shape = CircleShape, elevation = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    TextButton(
                        onClick = onNextDay,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Text(
                            "›",
                            style = MaterialTheme.typography.titleLarge,
                            color = scheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // ── Entry list ──────────────────────────────────────────────────────
        if (state.entries.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .softCard(shape = RoundedCornerShape(22.dp), elevation = 4.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No entries logged this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.entries, key = { it.id }) { entry ->
                    EntryRow(entry, onDelete)
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: WaterEntry, onDelete: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val iconLabel = sourceLabel(entry.source)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Tinted icon circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(scheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = sourceEmoji(entry.source),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        // Time + label column
        val localDt = entry.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
        val hh = localDt.hour.toString().padStart(2, '0')
        val mm = localDt.minute.toString().padStart(2, '0')
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$hh:$mm",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            Text(
                text = iconLabel,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }

        // Amount
        Text(
            text = "${entry.amountMl} ml",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )

        Spacer(Modifier.width(4.dp))

        // Delete affordance
        TextButton(onClick = { onDelete(entry.id) }) {
            Text(
                "✕",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.error,
            )
        }
    }
}

private fun sourceLabel(source: EntrySource): String = when (source) {
    EntrySource.QUICK_ADD -> "Quick add"
    EntrySource.CUSTOM    -> "Custom"
    EntrySource.REMINDER  -> "Reminder"
    EntrySource.IMPORTED  -> "Imported"
}

private fun sourceEmoji(source: EntrySource): String = when (source) {
    EntrySource.QUICK_ADD -> "💧"
    EntrySource.CUSTOM    -> "🥤"
    EntrySource.REMINDER  -> "🔔"
    EntrySource.IMPORTED  -> "📥"
}
