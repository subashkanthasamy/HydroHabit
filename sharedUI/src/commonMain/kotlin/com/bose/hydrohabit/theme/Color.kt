package com.bose.hydrohabit.theme

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
