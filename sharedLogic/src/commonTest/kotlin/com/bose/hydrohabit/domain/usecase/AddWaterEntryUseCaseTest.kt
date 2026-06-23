package com.bose.hydrohabit.domain.usecase

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.time.atTime
import com.bose.hydrohabit.domain.model.EntrySource
import com.bose.hydrohabit.testutil.FixedTimeProvider
import com.bose.hydrohabit.testutil.FakeWaterEntryRepository
import com.bose.hydrohabit.testutil.SequentialIdGenerator
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddWaterEntryUseCaseTest {

    private val date = LocalDate(2026, 6, 18)
    private val now = date.atTime(LocalTime(12, 0), TimeZone.UTC)
    private val repo = FakeWaterEntryRepository()
    private val useCase = AddWaterEntryUseCase(repo, FixedTimeProvider(now), SequentialIdGenerator("entry"))

    @Test
    fun addsEntryWithDerivedDateAndId() = runTest {
        val result = useCase(amountMl = 250, source = EntrySource.QUICK_ADD)
        assertTrue(result is AppResult.Success)
        val entry = (result as AppResult.Success).value
        assertEquals(250, entry.amountMl)
        assertEquals(date, entry.date)
        assertEquals("entry-0", entry.id)
        assertEquals(1, repo.state.value.size)
    }

    @Test
    fun rejectsNonPositiveAmount() = runTest {
        val result = useCase(amountMl = 0)
        assertTrue(result is AppResult.Failure)
        assertTrue(repo.state.value.isEmpty())
    }
}
