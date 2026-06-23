package com.bose.hydrohabit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bose.hydrohabit.components.BottomNavBar
import com.bose.hydrohabit.components.NavItem
import com.bose.hydrohabit.components.QuickAddSheet
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
import com.bose.hydrohabit.theme.GlassyBackground

private enum class Tab(val label: String, val icon: String) {
    HOME("Home", "🏠"),
    HISTORY("History", "📜"),
    ANALYTICS("Stats", "📊"),
    SETTINGS("Settings", "⚙️"),
}

/** Hosts the four feature screens behind a bottom navigation bar (no nav library). */
@Composable
fun MainScreen(
    homeStore: HomeStore,
    historyStore: HistoryStore,
    analyticsStore: AnalyticsStore,
    achievementsStore: AchievementsStore,
    settingsStore: SettingsStore,
    soundPlayer: com.bose.hydrohabit.util.SoundPlayer,
    onCreateProfile: (Double, Int) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var showAchievements by rememberSaveable { mutableStateOf(false) }
    var showQuickAdd by rememberSaveable { mutableStateOf(false) }

    GlassyBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavBar(
                    items = listOf(
                        NavItem("HOME", "Home", "🏠"),
                        NavItem("HISTORY", "History", "📜"),
                        NavItem("ANALYTICS", "Stats", "📊"),
                        NavItem("SETTINGS", "Settings", "⚙️"),
                    ),
                    selectedId = tab.name,
                    onSelect = { tab = Tab.valueOf(it) },
                    onFabClick = { showQuickAdd = true },
                )
            },
        ) { padding ->
            val content = Modifier.padding(padding)
            Box {
                when (tab) {
                    Tab.HOME -> {
                        val state by homeStore.state.collectAsState()
                        HomeScreen(
                            state = state,
                            onQuickAdd = { homeStore.dispatch(HomeIntent.AddQuickAdd(it)) },
                            onAddCustom = { homeStore.dispatch(HomeIntent.AddWater(it)) },
                            onCreateProfile = onCreateProfile,
                            onDismissUnlocked = { homeStore.dispatch(HomeIntent.ClearUnlocked) },
                            onShowAchievements = { showAchievements = true },
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
                    Tab.SETTINGS -> {
                        val state by settingsStore.state.collectAsState()
                        SettingsScreen(
                            state = state,
                            soundPlayer = soundPlayer,
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

                if (showAchievements) {
                    val achState by achievementsStore.state.collectAsState()
                    AchievementsScreen(
                        state = achState,
                        onBack = { showAchievements = false },
                        modifier = content,
                    )
                }

                if (showQuickAdd) {
                    val homeState by homeStore.state.collectAsState()
                    QuickAddSheet(
                        options = homeState.quickAddOptions,
                        onPick = { homeStore.dispatch(HomeIntent.AddQuickAdd(it)); showQuickAdd = false },
                        onCustom = { homeStore.dispatch(HomeIntent.AddWater(it)); showQuickAdd = false },
                        onDismiss = { showQuickAdd = false },
                    )
                }
            }
        }
    }
}
