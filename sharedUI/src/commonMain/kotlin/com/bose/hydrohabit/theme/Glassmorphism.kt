package com.bose.hydrohabit.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Soft-UI card modifier. Renders a solid surface-filled rounded card with a
 * soft purple-tinted shadow. Replaces the old glassmorphic implementation.
 *
 * Params [borderWidth], [lightAlpha], and [darkAlpha] are retained for source
 * compatibility with existing call sites but are intentionally unused.
 */
@Composable
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 0.dp,                 // retained for source compat; unused
    lightAlpha: Float = 0.65f,              // retained for source compat; unused
    darkAlpha: Float = 0.12f,               // retained for source compat; unused
    shadowElevation: Dp = 10.dp,
): Modifier = this.softCard(shape = shape, elevation = shadowElevation)

/**
 * Core soft card modifier used by [glassCard]. Applies a purple-tinted shadow and
 * fills the card with [MaterialTheme.colorScheme.surface].
 */
@Composable
fun Modifier.softCard(
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 10.dp,
): Modifier {
    val spot = Color(0xFF4C3C8C)
    return this
        .shadow(elevation = elevation, shape = shape, ambientColor = spot, spotColor = spot)
        .background(color = MaterialTheme.colorScheme.surface, shape = shape)
        .clip(shape)
}

/**
 * Screen-wide background container that draws a flat lilac fill with faint
 * sparkle accent circles. Replaces the old glowing-blob glassmorphic background.
 */
@Composable
fun GlassyBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sparkle = scheme.primary.copy(alpha = 0.10f)
            drawCircle(sparkle, radius = size.minDimension * 0.012f, center = Offset(size.width * 0.12f, size.height * 0.08f))
            drawCircle(sparkle, radius = size.minDimension * 0.016f, center = Offset(size.width * 0.88f, size.height * 0.06f))
            drawCircle(sparkle, radius = size.minDimension * 0.010f, center = Offset(size.width * 0.78f, size.height * 0.30f))
        }
        content()
    }
}
