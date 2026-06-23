package com.bose.hydrohabit.presentation.history

import com.bose.hydrohabit.domain.model.WaterEntry
import kotlinx.datetime.LocalDate

data class HistoryState(
    val date: LocalDate? = null,
    val isLoading: Boolean = true,
    val entries: List<WaterEntry> = emptyList(),
    val dailyTotalMl: Int = 0,
)

sealed interface HistoryIntent {
    data class SelectDate(val date: LocalDate) : HistoryIntent
    data object PreviousDay : HistoryIntent
    data object NextDay : HistoryIntent
    data class DeleteEntry(val id: String) : HistoryIntent
    data class EditEntry(val entry: WaterEntry) : HistoryIntent
}
