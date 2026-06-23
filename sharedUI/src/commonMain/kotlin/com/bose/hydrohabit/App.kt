package com.bose.hydrohabit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bose.hydrohabit.presentation.achievements.AchievementsState
import com.bose.hydrohabit.presentation.achievements.AchievementsStore
import com.bose.hydrohabit.presentation.analytics.AnalyticsStore
import com.bose.hydrohabit.presentation.history.HistoryStore
import com.bose.hydrohabit.presentation.home.HomeState
import com.bose.hydrohabit.presentation.home.HomeStore
import com.bose.hydrohabit.presentation.settings.SettingsStore
import com.bose.hydrohabit.theme.HydroTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme

/**
 * App entry composable. The host (`MainActivity`) creates each store with a lifecycle scope and
 * supplies [onCreateProfile], wired to the use-case layer.
 */
@Composable
fun App(
    homeStore: HomeStore,
    historyStore: HistoryStore,
    analyticsStore: AnalyticsStore,
    achievementsStore: AchievementsStore,
    settingsStore: SettingsStore,
    soundPlayer: com.bose.hydrohabit.util.SoundPlayer,
    onCreateProfile: (Double, Int) -> Unit,
) {
    val settingsState by settingsStore.state.collectAsState()
    val settings = settingsState.reminderSettings
    val darkTheme = when (settings.themeMode.uppercase()) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }
    
    HydroTheme(darkTheme = darkTheme, accentColor = settings.accentColor) {
        MainScreen(
            homeStore = homeStore,
            historyStore = historyStore,
            analyticsStore = analyticsStore,
            achievementsStore = achievementsStore,
            settingsStore = settingsStore,
            soundPlayer = soundPlayer,
            onCreateProfile = onCreateProfile,
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HydroTheme {
        HomeScreen(
            state = HomeState(isLoading = false),
            onQuickAdd = {},
            onAddCustom = {},
            onCreateProfile = { _, _ -> },
        )
    }
}

@Preview
@Composable
fun AchievementsScreenPreview() {
    HydroTheme {
        AchievementsScreen(state = AchievementsState(isLoading = false))
    }
}
