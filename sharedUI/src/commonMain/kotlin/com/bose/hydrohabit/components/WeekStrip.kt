package com.bose.hydrohabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * A Sun–Sat week strip showing the 7 days of the week containing [today].
 * Today's day is highlighted with a filled primary circle; other days are shown
 * with a surfaceVariant circle. Tapping a day invokes [onDayClick].
 */
@Composable
fun WeekStrip(
    today: LocalDate,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    // kotlinx DayOfWeek: MONDAY=1..SUNDAY=7 via isoDayNumber. Map to Sun=0, Mon=1, …, Sat=6.
    val daysFromSunday = today.dayOfWeek.isoDayNumber % 7  // Sun=7%7=0, Mon=1%7=1, …, Sat=6%7=6
    val sunday = today.minus(daysFromSunday, DateTimeUnit.DAY)
    val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        repeat(7) { i ->
            val day = sunday.plus(i, DateTimeUnit.DAY)
            val selected = day == today
            // WS-1: minimumInteractiveComponentSize ensures ≥48dp touch target.
            // WS-2: clip(CircleShape) before clickable bounds the ripple to the circle.
            // X-1 / Semantics: selectable with Role.Tab announces selection state to screen readers.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onDayClick(day) },
                    ),
            ) {
                Text(
                    text = labels[i],
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) scheme.primary else scheme.onSurfaceVariant,
                )
                // WS-2: ripple is bounded to the circle via clip(CircleShape) on the Box.
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        // WS-3: solid surfaceVariant instead of surface.copy(alpha=0.6f) for
                        //        visible contrast on the lilac card background.
                        .background(
                            if (selected) scheme.primary else scheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${day.dayOfMonth}",
                        style = MaterialTheme.typography.labelLarge,
                        // WS-4: removed explicit fontWeight = FontWeight.Bold override;
                        //        labelLarge already carries the intended weight.
                        color = if (selected) scheme.onPrimary else scheme.onSurface,
                    )
                }
            }
        }
    }
}
