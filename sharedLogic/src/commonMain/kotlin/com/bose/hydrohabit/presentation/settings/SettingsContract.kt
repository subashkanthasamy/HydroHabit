package com.bose.hydrohabit.presentation.settings

import com.bose.hydrohabit.domain.model.ActivityLevel
import com.bose.hydrohabit.domain.model.Gender
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.model.UserProfile
import kotlinx.datetime.LocalTime

data class SettingsState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val reminderSettings: ReminderSettings = ReminderSettings.DEFAULT,
)

sealed interface SettingsIntent {
    data class SaveProfile(
        val weightKg: Double,
        val age: Int,
        val gender: Gender? = null,
        val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
        val wakeTime: LocalTime,
        val sleepTime: LocalTime,
    ) : SettingsIntent

    data class UpdateReminders(val settings: ReminderSettings) : SettingsIntent
}
