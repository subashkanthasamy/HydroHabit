package com.bose.hydrohabit.di

import com.bose.hydrohabit.data.database.DatabaseDriverFactory
import com.bose.hydrohabit.domain.reminders.NotificationPermissionController
import com.bose.hydrohabit.domain.reminders.PlatformReminderScheduler
import com.bose.hydrohabit.domain.usecase.CreateInitialProfileUseCase
import com.bose.hydrohabit.presentation.achievements.AchievementsStoreFactory
import com.bose.hydrohabit.presentation.achievements.AchievementsStoreNative
import com.bose.hydrohabit.presentation.analytics.AnalyticsStoreFactory
import com.bose.hydrohabit.presentation.analytics.AnalyticsStoreNative
import com.bose.hydrohabit.presentation.history.HistoryStoreFactory
import com.bose.hydrohabit.presentation.history.HistoryStoreNative
import com.bose.hydrohabit.presentation.home.HomeStoreFactory
import com.bose.hydrohabit.presentation.home.HomeStoreNative
import com.bose.hydrohabit.presentation.settings.SettingsStoreFactory
import com.bose.hydrohabit.presentation.settings.SettingsStoreNative
import com.bose.hydrohabit.reminders.IosNotificationPermissionController
import com.bose.hydrohabit.reminders.IosReminderScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val iosPlatformModule: Module = module {
    single { DatabaseDriverFactory() }
    single<PlatformReminderScheduler> { IosReminderScheduler() }
    single<NotificationPermissionController> { IosNotificationPermissionController() }
}

/** Call once from the iOS app startup (e.g. in iOSApp.init). Named `do…` to avoid Swift's `init`. */
fun doInitKoin() = startKoin {
    modules(sharedModules() + iosPlatformModule)
}

/**
 * Swift-friendly accessor for use cases. Swift can't resolve Koin generics, so this exposes the
 * use cases it needs as plain properties.
 */
class KoinHelper : KoinComponent {
    private val homeStoreFactory: HomeStoreFactory by inject()
    private val createInitialProfile: CreateInitialProfileUseCase by inject()
    private val historyStoreFactory: HistoryStoreFactory by inject()
    private val analyticsStoreFactory: AnalyticsStoreFactory by inject()
    private val achievementsStoreFactory: AchievementsStoreFactory by inject()
    private val settingsStoreFactory: SettingsStoreFactory by inject()

    /** Build fully-wired, scope-managed store facades for each SwiftUI tab. */
    fun createHomeStore(): HomeStoreNative = HomeStoreNative(homeStoreFactory, createInitialProfile)
    fun createHistoryStore(): HistoryStoreNative = HistoryStoreNative(historyStoreFactory)
    fun createAnalyticsStore(): AnalyticsStoreNative = AnalyticsStoreNative(analyticsStoreFactory)
    fun createAchievementsStore(): AchievementsStoreNative = AchievementsStoreNative(achievementsStoreFactory)
    fun createSettingsStore(): SettingsStoreNative = SettingsStoreNative(settingsStoreFactory)
}
