package com.bose.hydrohabit.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Brand mark for HydroHabit: an open progress ring (~75% sweep) enclosing a teardrop/droplet
 * shape. Scales to the canvas size via the 120-unit master geometry from the design brief.
 *
 * [tint] defaults to [MaterialTheme.colorScheme.primary] so it tints periwinkle in light mode
 * and light-periwinkle in dark mode automatically. No hardcoded color literals.
 */
@Composable
fun HydroLogo(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(
        modifier = modifier.semantics { contentDescription = "HydroHabit logo" },
    ) {
        val scale = size.minDimension / 120f

        // ── Ring arc ─────────────────────────────────────────────────────────
        // Stroke width: 8 units at 120-unit base
        val ringStroke = 8f * scale
        val halfStroke = ringStroke / 2f
        val arcTopLeft = Offset(halfStroke, halfStroke)
        val arcSize = Size(size.width - ringStroke, size.height - ringStroke)

        drawArc(
            color = tint,
            startAngle = -200f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = ringStroke, cap = StrokeCap.Round),
        )

        // ── Teardrop path ─────────────────────────────────────────────────────
        // Master coordinates (120-unit space):
        //   M60 26 C60 26 84 54 84 74 C84 87.3 73.3 98 60 98 C46.7 98 36 87.3 36 74 C36 54 60 26 60 26 Z
        val teardrop = Path().apply {
            moveTo(60f * scale, 26f * scale)
            cubicTo(
                60f * scale, 26f * scale,
                84f * scale, 54f * scale,
                84f * scale, 74f * scale,
            )
            cubicTo(
                84f * scale, 87.3f * scale,
                73.3f * scale, 98f * scale,
                60f * scale, 98f * scale,
            )
            cubicTo(
                46.7f * scale, 98f * scale,
                36f * scale, 87.3f * scale,
                36f * scale, 74f * scale,
            )
            cubicTo(
                36f * scale, 54f * scale,
                60f * scale, 26f * scale,
                60f * scale, 26f * scale,
            )
            close()
        }

        drawPath(path = teardrop, color = tint)

        // ── Inner water line (subtle wave clipped to teardrop) ────────────────
        // Draw at ~60% level inside the drop; at small sizes (<= 32dp-ish) it's subtle
        // enough not to muddy the mark, so we always render it.
        clipPath(teardrop) {
            val levelY = 74f * scale  // roughly mid-drop

            // Simple single-wave fill clipped to the teardrop
            val waveColor = tint.copy(alpha = 0.3f)
            val amp = 4f * scale
            val segW = size.width / 3f

            val wavePath = Path().apply {
                moveTo(0f, levelY)
                var x = 0f
                while (x < size.width + segW) {
                    quadraticTo(x + segW / 4f, levelY - amp, x + segW / 2f, levelY)
                    quadraticTo(x + 3 * segW / 4f, levelY + amp, x + segW, levelY)
                    x += segW
                }
                lineTo(x, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = wavePath, color = waveColor)
        }
    }
}
