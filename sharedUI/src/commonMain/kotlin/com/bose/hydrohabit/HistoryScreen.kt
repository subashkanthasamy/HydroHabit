package com.bose.hydrohabit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.presentation.history.HistoryState
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
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPreviousDay) { Text("‹") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.date?.toString() ?: "", fontWeight = FontWeight.Bold)
                Text("${state.dailyTotalMl} ml", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onNextDay) { Text("›") }
        }

        if (state.entries.isEmpty()) {
            Text("No entries logged this day.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.entries, key = { it.id }) { entry ->
                    EntryRow(entry, onDelete)
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: WaterEntry, onDelete: (String) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("${entry.amountMl} ml", fontWeight = FontWeight.Bold)
                val time = entry.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).time
                val hh = time.hour.toString().padStart(2, '0')
                val mm = time.minute.toString().padStart(2, '0')
                Text("$hh:$mm • ${entry.source.name.lowercase()}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { onDelete(entry.id) }) { Text("Delete") }
        }
    }
}
