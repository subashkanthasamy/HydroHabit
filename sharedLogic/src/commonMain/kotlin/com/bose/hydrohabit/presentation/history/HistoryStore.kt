package com.bose.hydrohabit.presentation.history

import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.domain.usecase.DeleteWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.EditWaterEntryUseCase
import com.bose.hydrohabit.domain.usecase.ObserveEntriesForDateUseCase
import com.bose.hydrohabit.presentation.mvi.MviStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * MVI store for the history screen. A selected-date [StateFlow] drives a [flatMapLatest] into the
 * day's entries, so changing the date transparently re-subscribes to the right reactive query.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryStore(
    private val scope: CoroutineScope,
    private val observeEntries: ObserveEntriesForDateUseCase,
    private val deleteEntry: DeleteWaterEntryUseCase,
    private val editEntry: EditWaterEntryUseCase,
    private val timeProvider: TimeProvider,
) : MviStore<HistoryState, HistoryIntent> {

    private val selectedDate = MutableStateFlow(timeProvider.today())
    private val _state = MutableStateFlow(HistoryState(date = selectedDate.value))
    override val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        selectedDate
            .flatMapLatest { date -> observeEntries(date).map { date to it } }
            .onEach { (date, entries) ->
                _state.value = HistoryState(
                    date = date,
                    isLoading = false,
                    entries = entries,
                    dailyTotalMl = entries.sumOf { it.amountMl },
                )
            }
            .launchIn(scope)
    }

    override fun dispatch(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.SelectDate -> selectedDate.value = intent.date
            HistoryIntent.PreviousDay -> selectedDate.value = selectedDate.value.minus(1, DateTimeUnit.DAY)
            HistoryIntent.NextDay -> selectedDate.value = selectedDate.value.plus(1, DateTimeUnit.DAY)
            is HistoryIntent.DeleteEntry -> scope.launch { deleteEntry(intent.id) }
            is HistoryIntent.EditEntry -> scope.launch { editEntry(intent.entry) }
        }
    }
}
