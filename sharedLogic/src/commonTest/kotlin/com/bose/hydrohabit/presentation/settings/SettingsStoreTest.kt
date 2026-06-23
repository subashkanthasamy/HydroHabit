package com.bose.hydrohabit.presentation.settings

import com.bose.hydrohabit.core.time.atTime
import com.bose.hydrohabit.domain.engine.HydrationGoalCalculator
import com.bose.hydrohabit.domain.engine.ReminderScheduler
import com.bose.hydrohabit.domain.model.ReminderSettings
import com.bose.hydrohabit.domain.usecase.ComputeReminderScheduleUseCase
import com.bose.hydrohabit.domain.usecase.GetReminderSettingsUseCase
import com.bose.hydrohabit.domain.usecase.GetUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.RescheduleRemindersUseCase
import com.bose.hydrohabit.domain.usecase.SaveUserProfileUseCase
import com.bose.hydrohabit.domain.usecase.UpdateReminderSettingsUseCase
import com.bose.hydrohabit.testutil.FakeDailyGoalRepository
import com.bose.hydrohabit.testutil.FakeReminderSettingsRepository
import com.bose.hydrohabit.testutil.FakeUserProfileRepository
import com.bose.hydrohabit.testutil.FakeWaterEntryRepository
import com.bose.hydrohabit.testutil.FixedTimeProvider
import com.bose.hydrohabit.testutil.RecordingPlatformReminderScheduler
import com.bose.hydrohabit.testutil.SequentialIdGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStoreTest {

    private val today = LocalDate(2026, 6, 18)
    private val now = today.atTime(LocalTime(12, 0), TimeZone.UTC)

    private fun store(
        profileRepo: FakeUserProfileRepository,
        goalRepo: FakeDailyGoalRepository,
        settingsRepo: FakeReminderSettingsRepository,
        scheduler: RecordingPlatformReminderScheduler,
    ): SettingsStore {
        val tp = FixedTimeProvider(now)
        val entryRepo = FakeWaterEntryRepository()
        val compute = ComputeReminderScheduleUseCase(
            settingsRepo, goalRepo, entryRepo, ReminderScheduler(SequentialIdGenerator("r")), tp,
        )
        return SettingsStore(
            CoroutineScope(UnconfinedTestDispatcher()),
            GetUserProfileUseCase(profileRepo),
            GetReminderSettingsUseCase(settingsRepo),
            SaveUserProfileUseCase(profileRepo, goalRepo, HydrationGoalCalculator(), tp),
            UpdateReminderSettingsUseCase(settingsRepo),
            RescheduleRemindersUseCase(compute, settingsRepo, scheduler),
            tp,
            SequentialIdGenerator("p"),
        )
    }

    @Test
    fun saveProfileCreatesProfileAndRecalculatesGoal() = runTest {
        val profileRepo = FakeUserProfileRepository()
        val goalRepo = FakeDailyGoalRepository()
        val store = store(profileRepo, goalRepo, FakeReminderSettingsRepository(ReminderSettings.DEFAULT), RecordingPlatformReminderScheduler())

        store.dispatch(
            SettingsIntent.SaveProfile(
                weightKg = 70.0, age = 30, gender = com.bose.hydrohabit.domain.model.Gender.MALE,
                wakeTime = LocalTime(7, 0), sleepTime = LocalTime(23, 0),
            ),
        )

        assertEquals(70.0, profileRepo.getProfile()?.weightKg)
        assertEquals(2940, goalRepo.getGoal(today)?.targetMl)
    }

    @Test
    fun disablingRemindersCancelsScheduledOnes() = runTest {
        val settingsRepo = FakeReminderSettingsRepository(ReminderSettings.DEFAULT)
        val scheduler = RecordingPlatformReminderScheduler()
        val store = store(FakeUserProfileRepository(), FakeDailyGoalRepository(), settingsRepo, scheduler)

        store.dispatch(SettingsIntent.UpdateReminders(ReminderSettings.DEFAULT.copy(enabled = false)))

        assertEquals(false, settingsRepo.getSettings().enabled)
        assertTrue(scheduler.cancelled)
    }
}
