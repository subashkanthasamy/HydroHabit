package com.bose.hydrohabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A single bar datum for [BarChart].
 *
 * @param label      Short label displayed beneath the bar (e.g. "Mon").
 * @param fraction   Fill fraction in range 0f..1f (0 = empty, 1 = full height).
 * @param highlighted When true the bar uses the primary→secondary gradient; otherwise surfaceVariant.
 */
data class BarDatum(val label: String, val fraction: Float, val highlighted: Boolean = false)

/**
 * Renders a horizontal row of vertical bars with rounded tops, each backed by a
 * gradient fill derived from [MaterialTheme.colorScheme] tokens so it adapts to
 * both light and dark themes.
 *
 * @param bars     List of [BarDatum] values to render.
 * @param modifier Optional [Modifier] applied to the root [Row].
 * @param height   Total height of the chart (bars + labels).
 */
@Composable
fun BarChart(bars: List<BarDatum>, modifier: Modifier = Modifier, height: Dp = 108.dp) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { b ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                val frac = b.fraction.coerceIn(0f, 1f).coerceAtLeast(0.04f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(frac)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                if (b.highlighted) listOf(scheme.secondary, scheme.primary)
                                else listOf(scheme.surfaceVariant, scheme.surfaceVariant.copy(alpha = 0.7f))
                            )
                        )
                )
                Spacer(Modifier.height(8.dp))
                Text(b.label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
        }
    }
}
