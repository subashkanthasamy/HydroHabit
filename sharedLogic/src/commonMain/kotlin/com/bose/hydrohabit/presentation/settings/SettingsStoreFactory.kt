package com.bose.hydrohabit.presentation.settings

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.core.util.IdGenerator
import com.bose.hydrohabit.domain.usecase.GetReminderSettingsUseCase
import com.bose.hydrohabit.domain.usecase.GetUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.RescheduleRemindersUseCase
import com.bose.hydrohabit.domain.usecase.SaveUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.UpdateReminderSettingsUseCase
import kotlinx.coroutines.CoroutineScope

class SettingsStoreFactory(
    private val getUserProfile: GetUserProfileUseCase,
    private val getReminderSettings: GetReminderSettingsUseCase,
    private val saveUserProfile: SaveUserProfileUseCase,
    private val updateReminderSettings: UpdateReminderSettingsUseCase,
    private val rescheduleReminders: RescheduleRemindersUseCase,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
) {
    fun create(scope: CoroutineScope): SettingsStore = SettingsStore(
        scope, getUserProfile, getReminderSettings, saveUserProfile,
        updateReminderSettings, rescheduleReminders, timeProvider, idGenerator,
    )
}
