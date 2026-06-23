package com.bose.hydrohabit.presentation.history

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.usecase.DeleteWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.EditWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.ObserveEntriesForDateUseCase
import kotlinx.coroutines.CoroutineScope

class HistoryStoreFactory(
    private val observeEntries: ObserveEntriesForDateUseCase,
    private val deleteEntry: DeleteWaterEntryUseCase,
    private val editEntry: EditWaterEntryUseCase,
    private val timeProvider: TimeProvider,
) {
    fun create(scope: CoroutineScope): HistoryStore =
        HistoryStore(scope, observeEntries, deleteEntry, editEntry, timeProvider)
}
