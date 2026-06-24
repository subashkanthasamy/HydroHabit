package com.bose.hydrohabit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.bose.hydrohabit.domain.usecase.CreateInitialProfileUseCase
import com.bose.hydrohabit.presentation.achievements.AchievementsStoreFactory
import com.bose.hydrohabit.presentation.analytics.AnalyticsStoreFactory
import com.bose.hydrohabit.presentation.history.HistoryStoreFactory
import com.bose.hydrohabit.presentation.home.HomeStoreFactory
import com.bose.hydrohabit.presentation.settings.SettingsStoreFactory
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* result ignored */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        val createProfile: CreateInitialProfileUseCase = get()
        val homeStore = get<HomeStoreFactory>().create(lifecycleScope)
        val historyStore = get<HistoryStoreFactory>().create(lifecycleScope)
        val analyticsStore = get<AnalyticsStoreFactory>().create(lifecycleScope)
        val achievementsStore = get<AchievementsStoreFactory>().create(lifecycleScope)
        val settingsStore = get<SettingsStoreFactory>().create(lifecycleScope)

        val soundPlayer: com.bose.hydrohabit.util.SoundPlayer = get()

        setContent {
            App(
                homeStore = homeStore,
                historyStore = historyStore,
                analyticsStore = analyticsStore,
                achievementsStore = achievementsStore,
                settingsStore = settingsStore,
                soundPlayer = soundPlayer,
                onCreateProfile = { weightKg, age ->
                    // HomeStore reschedules reminders automatically once the goal appears.
                    lifecycleScope.launch { createProfile(weightKg, age) }
                },
            )
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
