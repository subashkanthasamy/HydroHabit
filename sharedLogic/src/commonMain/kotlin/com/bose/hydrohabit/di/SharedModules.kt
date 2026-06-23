package com.bose.hydrohabit.di

import com.bose.hydrohabit.core.time.SystemTimeProvider
import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.core.util.IdGenerator
import com.bose.hydrohabit.core.util.UuidGenerator
import com.bose.hydrohabit.data.database.createDatabase
import com.bose.hydrohabit.data.repository.AchievementRepositoryImpl
import com.bose.hydrohabit.data.repository.AnalyticsRepositoryImpl
import com.bose.hydrohabit.data.repository.DailyGoalRepositoryImpl
import com.bose.hydrohabit.data.repository.ReminderSettingsRepositoryImpl
import com.bose.hydrohabit.data.repository.StreakRepositoryImpl
import com.bose.hydrohabit.data.repository.UserProfileRepositoryImpl
import com.bose.hydrohabit.data.repository.WaterEntryRepositoryImpl
import com.bose.hydrohabit.domain.engine.AchievementEvaluator
import com.bose.hydrohabit.domain.engine.AnalyticsCalculator
import com.bose.hydrohabit.domain.engine.HydrationGoalCalculator
import com.bose.hydrohabit.domain.engine.ReminderScheduler
import com.bose.hydrohabit.domain.engine.StreakCalculator
import com.bose.hydrohabit.domain.repository.AchievementRepository
import com.bose.hydrohabit.domain.repository.AnalyticsRepository
import com.bose.hydrohabit.domain.repository.DailyGoalRepository
import com.bose.hydrohabit.domain.repository.ReminderSettingsRepository
import com.bose.hydrohabit.domain.repository.StreakRepository
import com.bose.hydrohabit.domain.repository.UserProfileRepository
import com.bose.hydrohabit.domain.repository.WaterEntryRepository
import com.bose.hydrohabit.domain.usecase.AddWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.CalculateDailyGoalUseCase
import com.bose.hydrohabit.domain.usecase.ComputeReminderScheduleUseCase
import com.bose.hydrohabit.domain.usecase.CreateInitialProfileUseCase
import com.bose.hydrohabit.domain.usecase.DeleteWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.EditWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.EvaluateAchievementsUseCase
import com.bose.hydrohabit.domain.usecase.GenerateAnalyticsReportUseCase
import com.bose.hydrohabit.domain.usecase.GetHydrationInsightsUseCase
import com.bose.hydrohabit.domain.usecase.GetQuickAddOptionsUseCase
import com.bose.hydrohabit.domain.usecase.GetReminderSettingsUseCase
import com.bose.hydrohabit.domain.usecase.GetUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.ObserveAchievementsUseCase
import com.bose.hydrohabit.domain.usecase.ObserveDailyGoalUseCase
import com.bose.hydrohabit.domain.usecase.ObserveDailyProgressUseCase
import com.bose.hydrohabit.domain.usecase.ObserveEntriesForDateUseCase
import com.bose.hydrohabit.domain.usecase.ObserveStreakUseCase
import com.bose.hydrohabit.domain.usecase.RecalculateStreakUseCase
import com.bose.hydrohabit.domain.usecase.RescheduleRemindersUseCase
import com.bose.hydrohabit.domain.usecase.SaveUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.SetManualGoalUseCase
import com.bose.hydrohabit.domain.usecase.UpdateReminderSettingsUseCase
import com.bose.hydrohabit.presentation.achievements.AchievementsStoreFactory
import com.bose.hydrohabit.presentation.analytics.AnalyticsStoreFactory
import com.bose.hydrohabit.presentation.history.HistoryStoreFactory
import com.bose.hydrohabit.presentation.home.HomeStoreFactory
import com.bose.hydrohabit.presentation.settings.SettingsStoreFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/** Engines, clock, id generator, and the SQLDelight database wrapper. */
val coreModule: Module = module {
    single<TimeProvider> { SystemTimeProvider() }
    single<IdGenerator> { UuidGenerator() }
    single { HydrationGoalCalculator() }
    single { ReminderScheduler(get()) }
    single { StreakCalculator() }
    single { AchievementEvaluator(get()) }
    single { AnalyticsCalculator(get(), get()) }
}

/** Repository implementations. The DatabaseDriverFactory comes from the platform module. */
val dataModule: Module = module {
    single { createDatabase(get()) }
    single<UserProfileRepository> { UserProfileRepositoryImpl(get()) }
    single<WaterEntryRepository> { WaterEntryRepositoryImpl(get()) }
    single<DailyGoalRepository> { DailyGoalRepositoryImpl(get()) }
    single<ReminderSettingsRepository> { ReminderSettingsRepositoryImpl(get()) }
    single<StreakRepository> { StreakRepositoryImpl(get()) }
    single<AchievementRepository> { AchievementRepositoryImpl(get()) }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }
}

/** Use cases — stateless, so provided as factories. */
val domainModule: Module = module {
    factory { SaveUserProfileUseCase(get(), get(), get(), get()) }
    factory { GetUserProfileUseCase(get()) }
    factory { CreateInitialProfileUseCase(get(), get(), get()) }
    factory { CalculateDailyGoalUseCase(get(), get(), get()) }
    factory { SetManualGoalUseCase(get()) }
    factory { ObserveDailyGoalUseCase(get()) }
    factory { AddWaterEntryUseCase(get(), get(), get()) }
    factory { EditWaterEntryUseCase(get()) }
    factory { DeleteWaterEntryUseCase(get()) }
    factory { ObserveEntriesForDateUseCase(get()) }
    factory { GetQuickAddOptionsUseCase() }
    factory { ObserveDailyProgressUseCase(get(), get()) }
    factory { GetReminderSettingsUseCase(get()) }
    factory { UpdateReminderSettingsUseCase(get()) }
    factory { ComputeReminderScheduleUseCase(get(), get(), get(), get(), get()) }
    factory { RescheduleRemindersUseCase(get(), get(), get()) }
    factory { ObserveStreakUseCase(get()) }
    factory { RecalculateStreakUseCase(get(), get(), get(), get()) }
    factory { ObserveAchievementsUseCase(get()) }
    factory { EvaluateAchievementsUseCase(get(), get(), get(), get(), get(), get()) }
    factory { GenerateAnalyticsReportUseCase(get(), get()) }
    factory { GetHydrationInsightsUseCase(get(), get()) }
}

/** Presentation-layer MVI store factories. */
val presentationModule: Module = module {
    single { HomeStoreFactory(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { HistoryStoreFactory(get(), get(), get(), get()) }
    single { AnalyticsStoreFactory(get(), get()) }
    single { AchievementsStoreFactory(get(), get()) }
    single { SettingsStoreFactory(get(), get(), get(), get(), get(), get(), get()) }
}

/** Modules shared by both platforms. Platform modules add the driver factory + reminder ports. */
fun sharedModules(): List<Module> = listOf(coreModule, dataModule, domainModule, presentationModule)
