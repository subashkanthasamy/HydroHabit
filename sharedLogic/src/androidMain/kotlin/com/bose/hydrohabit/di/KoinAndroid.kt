package com.bose.hydrohabit.di

import android.content.Context
import com.bose.hydrohabit.data.database.DatabaseDriverFactory
import com.bose.hydrohabit.domain.reminders.NotificationPermissionController
import com.bose.hydrohabit.domain.reminders.PlatformReminderScheduler
import com.bose.hydrohabit.reminders.AndroidNotificationPermissionController
import com.bose.hydrohabit.reminders.AndroidReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val androidPlatformModule: Module = module {
    single { DatabaseDriverFactory(androidContext()) }
    single<PlatformReminderScheduler> { AndroidReminderScheduler(androidContext()) }
    single<NotificationPermissionController> { AndroidNotificationPermissionController(androidContext()) }
}

/** Call once from Application.onCreate(), passing the application context. */
fun initKoin(context: Context, appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    androidContext(context)
    modules(sharedModules() + androidPlatformModule)
}
