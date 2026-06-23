package com.bose.hydrohabit.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Lavender soft-UI brand palette — periwinkle accents on a pale lilac ground.
private val Periwinkle = Color(0xFF6C5CE7)      // primary, AA on white (4.86:1 verified)
private val PeriwinkleBright = Color(0xFF7B6FE8) // decorative brand-2 (arcs, FAB)
private val Ink = Color(0xFF1E1B3A)              // primary text on light

val LavenderLightBackground = Color(0xFFEFEDFB)
val LavenderLightSurface = Color(0xFFFFFFFF)
val LavenderDarkBackground = Color(0xFF15131F)
val LavenderDarkSurface = Color(0xFF211E33)

val HydroLightColors = lightColorScheme(
    primary = Periwinkle,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDEAFB),
    onPrimaryContainer = Color(0xFF221A52),
    secondary = PeriwinkleBright,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9D3F5),
    onSecondaryContainer = Color(0xFF2A2455),
    tertiary = Color(0xFF7A6E9E),
    background = LavenderLightBackground,
    onBackground = Ink,
    surface = LavenderLightSurface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE4DFF7),
    onSurfaceVariant = Color(0xFF6E6A8F),
    error = Color(0xFFBA1A1A),
)

val HydroDarkColors = darkColorScheme(
    primary = Color(0xFFA99CFF),
    onPrimary = Color(0xFF241B52),
    primaryContainer = Color(0xFF3A3270),
    onPrimaryContainer = Color(0xFFEDEAFB),
    secondary = Color(0xFFB9AEF0),
    onSecondary = Color(0xFF241B52),
    secondaryContainer = Color(0xFF2C2746),
    onSecondaryContainer = Color(0xFFE7E4F5),
    tertiary = Color(0xFFC9C0EA),
    background = LavenderDarkBackground,
    onBackground = Color(0xFFE7E4F5),
    surface = LavenderDarkSurface,
    onSurface = Color(0xFFE7E4F5),
    surfaceVariant = Color(0xFF2E2A45),
    onSurfaceVariant = Color(0xFFA7A2C4),
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
        Color(0xFF6C5CE7)
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
            onPrimary = Color(0xFF241B52),
            primaryContainer = hslToColor(h, s, 0.25f),
            onPrimaryContainer = hslToColor(h, s, 0.90f),
            secondary = secondaryColor,
            onSecondary = Color(0xFF241B52),
            secondaryContainer = hslToColor((h + 30f) % 360f, s, 0.20f),
            onSecondaryContainer = hslToColor((h + 30f) % 360f, s, 0.85f),
            tertiary = hslToColor((h + 180f) % 360f, s * 0.5f, 0.70f),
            background = LavenderDarkBackground,
            onBackground = Color(0xFFE7E4F5),
            surface = LavenderDarkSurface,
            onSurface = Color(0xFFE7E4F5),
            surfaceVariant = Color(0xFF2E2A45),
            onSurfaceVariant = Color(0xFFA7A2C4),
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
            background = LavenderLightBackground,
            onBackground = Ink,
            surface = LavenderLightSurface,
            onSurface = Ink,
            surfaceVariant = Color(0xFFE4DFF7),
            onSurfaceVariant = Color(0xFF6E6A8F),
            error = Color(0xFFBA1A1A),
        )
    }
}
