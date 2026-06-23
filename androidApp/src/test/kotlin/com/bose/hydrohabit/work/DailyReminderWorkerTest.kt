package com.bose.hydrohabit.work

import android.app.Application
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.DomainError
import com.bose.hydrohabit.core.time.SystemTimeProvider
import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.usecase.CalculateDailyGoalUseCase
import com.bose.hydrohabit.domain.usecase.RescheduleRemindersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DailyReminderWorkerTest {

    private val context get() = RuntimeEnvironment.getApplication()

    private val calculateDailyGoal = mockk<CalculateDailyGoalUseCase>()
    private val rescheduleReminders = mockk<RescheduleRemindersUseCase>()

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single<TimeProvider> { SystemTimeProvider() }
                    single { calculateDailyGoal }
                    single { rescheduleReminders }
                },
            )
        }
    }

    @After
    fun tearDown() = stopKoin()

    @Test
    fun doWork_recalculatesGoalAndReschedules_returningSuccess() = runBlocking {
        coEvery { calculateDailyGoal.invoke(any(), any()) } returns AppResult.Failure(DomainError.NotFound)
        coEvery { rescheduleReminders.invoke() } returns AppResult.Success(Unit)

        val worker = TestListenableWorkerBuilder<DailyReminderWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { calculateDailyGoal.invoke(any(), any()) }
        coVerify(exactly = 1) { rescheduleReminders.invoke() }
    }

    @Test
    fun doWork_returnsRetry_whenReschedulingThrows() = runBlocking {
        coEvery { calculateDailyGoal.invoke(any(), any()) } returns AppResult.Success(mockk())
        coEvery { rescheduleReminders.invoke() } throws RuntimeException("boom")

        val worker = TestListenableWorkerBuilder<DailyReminderWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun ensureScheduled_enqueuesUniquePeriodicWork() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        ReminderWorkScheduler.ensureScheduled(context)

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(ReminderWorkScheduler.DAILY_WORK_NAME).get()

        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.first().state)
    }

    @Test
    fun runNow_enqueuesOneTimeWork() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        ReminderWorkScheduler.runNow(context)

        val infos = WorkManager.getInstance(context)
            .getWorkInfosByTag(DailyReminderWorker::class.java.name).get()

        assertTrue(infos.isNotEmpty())
    }
}
