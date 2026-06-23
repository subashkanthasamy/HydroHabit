package com.bose.hydrohabit.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Animated circular hydration ring — the dashboard centerpiece. Sweeps from 0 to [progress]
 * (0f..1f) with an ease, drawing the track + a gradient progress arc and the percentage in the
 * middle.
 *
 * When [waveFill] is true the ring's interior also renders an animated wave filled to the
 * progress level, clipped to the inner circle, rendered behind the center text.
 */
@Composable
fun WaterRing(
    progress: Float,
    consumedMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier,
    waveFill: Boolean = false,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "waterRingProgress",
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val startColor = MaterialTheme.colorScheme.secondary
    val endColor = MaterialTheme.colorScheme.primary

    // Wave colors resolved from theme (no hardcoded literals).
    val waveColorBack = MaterialTheme.colorScheme.secondary
    val waveColorFront = MaterialTheme.colorScheme.primary

    // Infinite phase animation for wave.
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 3500, easing = LinearEasing)),
        label = "phase",
    )

    val percent = (progress.coerceIn(0f, 1f) * 100).toInt()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(232.dp)
            .semantics { contentDescription = "Hydration progress $percent percent: $consumedMl of $goalMl milliliters" },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            // Inner radius is half of the drawable area minus the stroke (the hollow interior).
            val innerR = (size.width - strokeWidth) / 2f

            // --- Wave fill (behind arc) ---
            if (waveFill) {
                val clipCircle = Path().apply {
                    addOval(
                        Rect(
                            center = center,
                            radius = innerR,
                        )
                    )
                }
                clipPath(clipCircle) {
                    val levelY = center.y - innerR + (2f * innerR) * (1f - animated)
                    // Back wave: slightly lower, secondary color, more transparent.
                    drawWave(
                        levelY = levelY + 6.dp.toPx(),
                        phase = phase + 0.5f,
                        color = waveColorBack.copy(alpha = 0.55f),
                        amp = 6.dp.toPx(),
                        width = size.width,
                        bottomY = size.height,
                    )
                    // Front wave: at level, primary color.
                    drawWave(
                        levelY = levelY,
                        phase = phase,
                        color = waveColorFront.copy(alpha = 0.9f),
                        amp = 6.dp.toPx(),
                        width = size.width,
                        bottomY = size.height,
                    )
                }
            }

            // --- Track arc (existing, unchanged) ---
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            // --- Progress arc (existing, unchanged) ---
            if (animated > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(startColor, endColor, startColor)),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }
        // Center text (existing, unchanged — naturally overlays the canvas).
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(animated * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "$consumedMl / $goalMl ml",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Draws a single wave fill from [levelY] down to [bottomY] across [width], with a sinusoidal
 * surface built from quadratic Bezier segments. [phase] (0f..1f) shifts the wave horizontally
 * for continuous animation. [amp] controls peak-to-trough amplitude.
 */
private fun DrawScope.drawWave(
    levelY: Float,
    phase: Float,
    color: Color,
    amp: Float,
    width: Float,
    bottomY: Float,
) {
    val segmentCount = 4 // number of full wave periods across the width
    val segmentWidth = width / segmentCount
    // Phase offset in pixels: full width shift over one cycle.
    val phaseOffset = phase * width

    val path = Path().apply {
        moveTo(-phaseOffset % segmentWidth, levelY)

        var x = -phaseOffset % segmentWidth
        // Extend slightly past the right edge so phase shift never reveals a gap.
        while (x < width + segmentWidth) {
            val cpX1 = x + segmentWidth / 4f
            val cpY1 = levelY - amp
            val endX1 = x + segmentWidth / 2f
            val endY1 = levelY
            quadraticTo(cpX1, cpY1, endX1, endY1)

            val cpX2 = endX1 + segmentWidth / 4f
            val cpY2 = levelY + amp
            val endX2 = endX1 + segmentWidth / 2f
            val endY2 = levelY
            quadraticTo(cpX2, cpY2, endX2, endY2)

            x = endX2
        }

        // Close path down to bottom to form a filled region.
        lineTo(x, bottomY)
        lineTo(-phaseOffset % segmentWidth, bottomY)
        close()
    }

    drawPath(path = path, color = color)
}
