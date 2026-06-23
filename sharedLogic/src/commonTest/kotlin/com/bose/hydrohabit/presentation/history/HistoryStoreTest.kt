package com.bose.hydrohabit.presentation.history

import com.bose.hydrohabit.core.time.atTime
import com.bose.hydrohabit.domain.model.EntrySource
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.domain.usecase.DeleteWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.EditWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.ObserveEntriesForDateUseCase
import com.bose.hydrohabit.testutil.FakeWaterEntryRepository
import com.bose.hydrohabit.testutil.FixedTimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryStoreTest {

    private val today = LocalDate(2026, 6, 18)
    private val now = today.atTime(LocalTime(12, 0), TimeZone.UTC)

    @Test
    fun showsTodayEntriesThenNavigatesToPreviousDay() = runTest {
        val repo = FakeWaterEntryRepository()
        repo.state.value = listOf(
            WaterEntry("a", 250, now, today, EntrySource.QUICK_ADD),
            WaterEntry("b", 500, now, today, EntrySource.CUSTOM),
        )
        val store = HistoryStore(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            ObserveEntriesForDateUseCase(repo),
            DeleteWaterEntryUseCase(repo),
            EditWaterEntryUseCase(repo),
            FixedTimeProvider(now),
        )

        assertEquals(today, store.state.value.date)
        assertEquals(2, store.state.value.entries.size)
        assertEquals(750, store.state.value.dailyTotalMl)

        store.dispatch(HistoryIntent.PreviousDay)
        assertEquals(today.minus(1, DateTimeUnit.DAY), store.state.value.date)
        assertEquals(0, store.state.value.dailyTotalMl)
    }

    @Test
    fun deleteRemovesEntryFromState() = runTest {
        val repo = FakeWaterEntryRepository()
        repo.state.value = listOf(WaterEntry("a", 250, now, today, EntrySource.QUICK_ADD))
        val store = HistoryStore(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            ObserveEntriesForDateUseCase(repo),
            DeleteWaterEntryUseCase(repo),
            EditWaterEntryUseCase(repo),
            FixedTimeProvider(now),
        )

        store.dispatch(HistoryIntent.DeleteEntry("a"))
        assertEquals(0, store.state.value.entries.size)
    }
}
