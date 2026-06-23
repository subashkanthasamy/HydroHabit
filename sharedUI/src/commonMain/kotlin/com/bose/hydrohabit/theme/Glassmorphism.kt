package com.bose.hydrohabit.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom glassmorphic modifier for Compose Multiplatform.
 * Applies a semi-transparent gradient fill, an inner-light reflecting border, and optional soft shadow.
 */
@Composable
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    lightAlpha: Float = 0.65f,
    darkAlpha: Float = 0.12f,
    shadowElevation: Dp = 0.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    val baseColor = if (isDark) Color(0xFF1E293B) else Color.White
    val containerColor = baseColor.copy(alpha = if (isDark) darkAlpha else lightAlpha)
    
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }

    return this
        .then(if (shadowElevation > 0.dp) Modifier.shadow(shadowElevation, shape) else Modifier)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    containerColor,
                    containerColor.copy(alpha = containerColor.alpha * 0.4f)
                )
            ),
            shape = shape
        )
        .border(
            width = borderWidth,
            brush = Brush.verticalGradient(
                colors = listOf(
                    borderColor,
                    borderColor.copy(alpha = borderColor.alpha * 0.1f)
                )
            ),
            shape = shape
        )
}

/**
 * A beautiful screen-wide container that draws glowing ambient blobs in the background.
 * Essential for rendering visible glassmorphic frosting and depth.
 */
@Composable
fun GlassyBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val baseBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val glow1 = if (isDark) Color(0xFF0EA5E9).copy(alpha = 0.12f) else Color(0xFF38BDF8).copy(alpha = 0.25f)
    val glow2 = if (isDark) Color(0xFF06B6D4).copy(alpha = 0.08f) else Color(0xFF22D3EE).copy(alpha = 0.18f)
    val glow3 = if (isDark) Color(0xFF6366F1).copy(alpha = 0.05f) else Color(0xFF818CF8).copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = glow1,
                radius = size.minDimension * 0.7f,
                center = Offset(size.width * 0.85f, size.height * 0.15f)
            )
            drawCircle(
                color = glow2,
                radius = size.minDimension * 0.6f,
                center = Offset(size.width * 0.1f, size.height * 0.75f)
            )
            drawCircle(
                color = glow3,
                radius = size.minDimension * 0.4f,
                center = Offset(size.width * 0.5f, size.height * 0.45f)
            )
        }
        content()
    }
}
