package com.bose.hydrohabit

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.bose.hydrohabit.domain.model.ActivityLevel
import com.bose.hydrohabit.presentation.achievements.AchievementsStore
import com.bose.hydrohabit.presentation.analytics.AnalyticsIntent
import com.bose.hydrohabit.presentation.analytics.AnalyticsStore
import com.bose.hydrohabit.presentation.history.HistoryIntent
import com.bose.hydrohabit.presentation.history.HistoryStore
import com.bose.hydrohabit.presentation.home.HomeIntent
import com.bose.hydrohabit.presentation.home.HomeStore
import com.bose.hydrohabit.presentation.settings.SettingsIntent
import com.bose.hydrohabit.presentation.settings.SettingsStore

private enum class Tab(val label: String, val icon: String) {
    HOME("Home", "🏠"),
    HISTORY("History", "📜"),
    ANALYTICS("Stats", "📊"),
    ACHIEVEMENTS("Awards", "🏆"),
    SETTINGS("Settings", "⚙️"),
}

/** Hosts the five feature screens behind a bottom navigation bar (no nav library). */
@Composable
fun MainScreen(
    homeStore: HomeStore,
    historyStore: HistoryStore,
    analyticsStore: AnalyticsStore,
    achievementsStore: AchievementsStore,
    settingsStore: SettingsStore,
    onCreateProfile: (Double, Int) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { entry ->
                    NavigationBarItem(
                        selected = tab == entry,
                        onClick = { tab = entry },
                        icon = { Text(entry.icon) },
                        label = { Text(entry.label) },
                    )
                }
            }
        },
    ) { padding ->
        val content = Modifier.padding(padding)
        when (tab) {
            Tab.HOME -> {
                val state by homeStore.state.collectAsState()
                HomeScreen(
                    state = state,
                    onQuickAdd = { homeStore.dispatch(HomeIntent.AddQuickAdd(it)) },
                    onAddCustom = { homeStore.dispatch(HomeIntent.AddWater(it)) },
                    onCreateProfile = onCreateProfile,
                    onDismissUnlocked = { homeStore.dispatch(HomeIntent.ClearUnlocked) },
                    modifier = content,
                )
            }
            Tab.HISTORY -> {
                val state by historyStore.state.collectAsState()
                HistoryScreen(
                    state = state,
                    onPreviousDay = { historyStore.dispatch(HistoryIntent.PreviousDay) },
                    onNextDay = { historyStore.dispatch(HistoryIntent.NextDay) },
                    onDelete = { historyStore.dispatch(HistoryIntent.DeleteEntry(it)) },
                    modifier = content,
                )
            }
            Tab.ANALYTICS -> {
                val state by analyticsStore.state.collectAsState()
                AnalyticsScreen(
                    state = state,
                    onSelectPeriod = { analyticsStore.dispatch(AnalyticsIntent.SelectPeriod(it)) },
                    modifier = content,
                )
            }
            Tab.ACHIEVEMENTS -> {
                val state by achievementsStore.state.collectAsState()
                AchievementsScreen(state = state, modifier = content)
            }
            Tab.SETTINGS -> {
                val state by settingsStore.state.collectAsState()
                SettingsScreen(
                    state = state,
                    onSaveProfile = { weightKg, age ->
                        val profile = state.profile
                        settingsStore.dispatch(
                            SettingsIntent.SaveProfile(
                                weightKg = weightKg,
                                age = age,
                                gender = profile?.gender,
                                activityLevel = profile?.activityLevel ?: ActivityLevel.MODERATE,
                                wakeTime = profile?.wakeTime ?: state.reminderSettings.wakeTime,
                                sleepTime = profile?.sleepTime ?: state.reminderSettings.sleepTime,
                            ),
                        )
                    },
                    onUpdateReminders = { settingsStore.dispatch(SettingsIntent.UpdateReminders(it)) },
                    modifier = content,
                )
            }
        }
    }
}
