package com.bose.hydrohabit.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Hydration-focused brand palette — cool blues & teals for a fresh, "water" feel.
private val Aqua = Color(0xFF006690) // Darkened for >4.5:1 contrast in light theme
private val AquaDark = Color(0xFF7FD1E8)
private val Teal = Color(0xFF006A75) // Darkened for >4.5:1 contrast in light theme
private val DeepBlue = Color(0xFF0B3D59)

val HydroLightColors = lightColorScheme(
    primary = Aqua,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEAF6),
    onPrimaryContainer = Color(0xFF00344A),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8ECF3),
    onSecondaryContainer = Color(0xFF00363C),
    tertiary = Color(0xFF4A6572),
    background = Color(0xFFF6FBFD),
    onBackground = DeepBlue,
    surface = Color(0xFFFFFFFF),
    onSurface = DeepBlue,
    surfaceVariant = Color(0xFFDDE7EC),
    onSurfaceVariant = Color(0xFF3F484D), // Refined for 7:1 contrast
    error = Color(0xFFBA1A1A),
)

val HydroDarkColors = darkColorScheme(
    primary = AquaDark,
    onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF004E6B),
    onPrimaryContainer = Color(0xFFCDEAF6),
    secondary = Color(0xFF4FD8E6),
    onSecondary = Color(0xFF00363C),
    secondaryContainer = Color(0xFF004F57),
    onSecondaryContainer = Color(0xFFB8ECF3),
    tertiary = Color(0xFFB1CBD9),
    background = Color(0xFF0E1416),
    onBackground = Color(0xFFDEE3E6),
    surface = Color(0xFF161D20),
    onSurface = Color(0xFFDEE3E6),
    surfaceVariant = Color(0xFF40484C),
    onSurfaceVariant = Color(0xFFCBD5E1), // Lightened for high contrast
    error = Color(0xFFFFB4AB),
)

fun parseHexColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#").trim()
    return try {
        val argb = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(argb or 0xFF000000)
        } else {
            Color(argb)
        }
    } catch (e: Exception) {
        Color(0xFF006690)
    }
}

fun Color.toHsl(outHsl: FloatArray = FloatArray(3)): FloatArray {
    val r = red
    val g = green
    val b = blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    var h = 0f
    var s = 0f
    val l = (max + min) / 2f

    if (max != min) {
        val d = max - min
        s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        h = when (max) {
            r -> (g - b) / d + (if (g < b) 6f else 0f)
            g -> (b - r) / d + 2f
            else -> (r - g) / d + 4f
        }
        h /= 6f
    }

    outHsl[0] = h * 360f
    outHsl[1] = s
    outHsl[2] = l
    return outHsl
}

fun hslToColor(h: Float, s: Float, l: Float, alpha: Float = 1f): Color {
    val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
    val p = 2f * l - q

    fun hue2rgb(t: Float): Float {
        var varT = t
        if (varT < 0f) varT += 1f
        if (varT > 1f) varT -= 1f
        if (varT < 1f / 6f) return p + (q - p) * 6f * varT
        if (varT < 1f / 2f) return q
        if (varT < 2f / 3f) return p + (q - p) * (2f / 3f - varT) * 6f
        return p
    }

    val r = hue2rgb(h / 360f + 1f / 3f)
    val g = hue2rgb(h / 360f)
    val b = hue2rgb(h / 360f - 1f / 3f)

    return Color(r, g, b, alpha)
}

fun generateDynamicColorScheme(accentColorHex: String, isDark: Boolean): ColorScheme {
    val baseColor = parseHexColor(accentColorHex)
    val hsl = baseColor.toHsl()
    val h = hsl[0]
    val s = hsl[1]
    val l = hsl[2]

    return if (isDark) {
        val primaryL = l.coerceAtLeast(0.75f)
        val primaryColor = hslToColor(h, s, primaryL)
        val secondaryColor = hslToColor((h + 30f) % 360f, s, primaryL - 0.05f)
        
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color(0xFF00344A),
            primaryContainer = hslToColor(h, s, 0.25f),
            onPrimaryContainer = hslToColor(h, s, 0.90f),
            secondary = secondaryColor,
            onSecondary = Color(0xFF00363C),
            secondaryContainer = hslToColor((h + 30f) % 360f, s, 0.20f),
            onSecondaryContainer = hslToColor((h + 30f) % 360f, s, 0.85f),
            tertiary = hslToColor((h + 180f) % 360f, s * 0.5f, 0.70f),
            background = Color(0xFF0E1416),
            onBackground = Color(0xFFDEE3E6),
            surface = Color(0xFF161D20),
            onSurface = Color(0xFFDEE3E6),
            surfaceVariant = Color(0xFF40484C),
            onSurfaceVariant = Color(0xFFCBD5E1),
            error = Color(0xFFFFB4AB),
        )
    } else {
        val primaryL = l.coerceAtMost(0.42f)
        val primaryColor = hslToColor(h, s, primaryL)
        val secondaryColor = hslToColor((h + 30f) % 360f, s, primaryL - 0.02f)
        
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = hslToColor(h, s, 0.90f),
            onPrimaryContainer = hslToColor(h, s, 0.15f),
            secondary = secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = hslToColor((h + 30f) % 360f, s, 0.92f),
            onSecondaryContainer = hslToColor((h + 30f) % 360f, s, 0.12f),
            tertiary = hslToColor((h + 180f) % 360f, s * 0.5f, 0.40f),
            background = Color(0xFFF6FBFD),
            onBackground = DeepBlue,
            surface = Color.White,
            onSurface = DeepBlue,
            surfaceVariant = Color(0xFFDDE7EC),
            onSurfaceVariant = Color(0xFF3F484D),
            error = Color(0xFFBA1A1A),
        )
    }
}
