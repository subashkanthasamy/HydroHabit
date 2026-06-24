package com.bose.hydrohabit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(scheme.surfaceVariant)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }

            // SC-2: animate background and text color transitions (~200ms)
            val bgColor by animateColorAsState(
                targetValue = if (selected) scheme.primary else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "segmentBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 200),
                label = "segmentText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    // SC-1: ensure minimum 48dp touch target height
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
                    // SC-4: use selectable with Role.Tab instead of clickable
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onSelect(i) }
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    // SC-3: unselected text uses onSurfaceVariant (animated above)
                    color = textColor
                )
            }
        }
    }
}
