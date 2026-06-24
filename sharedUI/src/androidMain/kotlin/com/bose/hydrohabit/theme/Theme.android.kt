package com.bose.hydrohabit.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun hydroColorScheme(darkTheme: Boolean, dynamicColor: Boolean, accentColor: String?): ColorScheme {
    if (accentColor != null) {
        return generateDynamicColorScheme(accentColor, darkTheme)
    }
    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        return if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    return if (darkTheme) HydroDarkColors else HydroLightColors
}
