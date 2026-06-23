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
    onCreateProfile: (Double, Int) -> Unit,
) {
    HydroTheme {
        MainScreen(
            homeStore = homeStore,
            historyStore = historyStore,
            analyticsStore = analyticsStore,
            achievementsStore = achievementsStore,
            settingsStore = settingsStore,
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
