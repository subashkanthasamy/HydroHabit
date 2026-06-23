package com.bose.hydrohabit.presentation.settings

import com.bose.hydrohabit.domain.model.ActivityLevel
import com.bose.hydrohabit.presentation.home.CancellationToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Swift-friendly facade over [SettingsStore]. */
class SettingsStoreNative(factory: SettingsStoreFactory) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val store = factory.create(scope)

    val currentState: SettingsState get() = store.state.value
    fun watch(onState: (SettingsState) -> Unit): CancellationToken =
        CancellationToken(store.state.onEach { onState(it) }.launchIn(scope))

    fun dispatch(intent: SettingsIntent) = store.dispatch(intent)

    /** Saves weight/age, preserving the existing profile's other fields (or sensible defaults). */
    fun saveProfile(weightKg: Double, age: Int) {
        val current = store.state.value
        val profile = current.profile
        store.dispatch(
            SettingsIntent.SaveProfile(
                weightKg = weightKg,
                age = age,
                gender = profile?.gender,
                activityLevel = profile?.activityLevel ?: ActivityLevel.MODERATE,
                wakeTime = profile?.wakeTime ?: current.reminderSettings.wakeTime,
                sleepTime = profile?.sleepTime ?: current.reminderSettings.sleepTime,
            ),
        )
    }

    fun setRemindersEnabled(enabled: Boolean) =
        store.dispatch(SettingsIntent.UpdateReminders(store.state.value.reminderSettings.copy(enabled = enabled)))

    fun setReminderInterval(minutes: Int) =
        store.dispatch(SettingsIntent.UpdateReminders(store.state.value.reminderSettings.copy(intervalMinutes = minutes)))

    fun close() = scope.cancel()
}
