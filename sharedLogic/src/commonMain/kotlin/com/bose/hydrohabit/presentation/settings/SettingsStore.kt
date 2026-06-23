package com.bose.hydrohabit.presentation.settings

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.core.util.IdGenerator
import com.bose.hydrohabit.domain.model.UnitSystem
import com.bose.hydrohabit.domain.model.UserProfile
import com.bose.hydrohabit.domain.usecase.GetReminderSettingsUseCase
import com.bose.hydrohabit.domain.usecase.GetUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.RescheduleRemindersUseCase
import com.bose.hydrohabit.domain.usecase.SaveUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.UpdateReminderSettingsUseCase
import com.bose.hydrohabit.presentation.mvi.MviStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * MVI store for settings. Observes profile + reminder settings; saving a profile preserves the
 * existing id/createdAt (so the goal recalculates against a stable identity), and changing reminder
 * settings reschedules the OS reminders.
 */
class SettingsStore(
    private val scope: CoroutineScope,
    getUserProfile: GetUserProfileUseCase,
    getReminderSettings: GetReminderSettingsUseCase,
    private val saveUserProfile: SaveUserProfileUseCase,
    private val updateReminderSettings: UpdateReminderSettingsUseCase,
    private val rescheduleReminders: RescheduleRemindersUseCase,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
) : MviStore<SettingsState, SettingsIntent> {

    private val _state = MutableStateFlow(SettingsState())
    override val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        combine(getUserProfile(), getReminderSettings()) { profile, settings ->
            _state.value.copy(isLoading = false, profile = profile, reminderSettings = settings)
        }.onEach { _state.value = it }.launchIn(scope)
    }

    override fun dispatch(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SaveProfile -> scope.launch {
                val now = timeProvider.now()
                val existing = _state.value.profile
                val profile = existing?.copy(
                    weightKg = intent.weightKg,
                    age = intent.age,
                    gender = intent.gender,
                    activityLevel = intent.activityLevel,
                    wakeTime = intent.wakeTime,
                    sleepTime = intent.sleepTime,
                    updatedAt = now,
                ) ?: UserProfile(
                    id = idGenerator.newId(),
                    weightKg = intent.weightKg,
                    age = intent.age,
                    gender = intent.gender,
                    activityLevel = intent.activityLevel,
                    wakeTime = intent.wakeTime,
                    sleepTime = intent.sleepTime,
                    unitSystem = UnitSystem.METRIC,
                    createdAt = now,
                    updatedAt = now,
                )
                saveUserProfile(profile)
            }

            is SettingsIntent.UpdateReminders -> scope.launch {
                updateReminderSettings(intent.settings)
                rescheduleReminders()
            }
        }
    }
}
