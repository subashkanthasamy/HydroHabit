package com.bose.hydrohabit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

/**
 * Resolves the active color scheme. On Android 12+ this returns the user's wallpaper-based dynamic
 * colors when [dynamicColor] is on; otherwise the HydroHabit brand palette. Other platforms always
 * use the brand palette.
 */
@Composable
expect fun hydroColorScheme(darkTheme: Boolean, dynamicColor: Boolean, accentColor: String?): ColorScheme

val HydroTypography = Typography()

/** App-wide Material 3 theme. Honors system dark mode and (on Android 12+) dynamic color. */
@Composable
fun HydroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    accentColor: String? = null,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = hydroColorScheme(darkTheme, dynamicColor, accentColor),
        typography = HydroTypography,
        content = content,
    )
}
