package com.bose.hydrohabit.domain.usecase

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.DomainError
import com.bose.hydrohabit.core.time.TimeProvider
import com.bose.hydrohabit.core.time.toLocalDate
import com.bose.hydrohabit.core.util.IdGenerator
import com.bose.hydrohabit.core.util.Volume
import com.bose.hydrohabit.domain.model.EntrySource
import com.bose.hydrohabit.domain.model.UnitSystem
import com.bose.hydrohabit.domain.model.WaterEntry
import com.bose.hydrohabit.domain.repository.WaterEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Logs a water entry. Supports both quick-add presets and custom amounts; the [at] instant
 * defaults to now, so imported/back-dated entries are also expressible.
 */
class AddWaterEntryUseCase(
    private val repository: WaterEntryRepository,
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
) {
    suspend operator fun invoke(
        amountMl: Int,
        source: EntrySource = EntrySource.CUSTOM,
        note: String? = null,
        at: Instant = timeProvider.now(),
    ): AppResult<WaterEntry> {
        if (amountMl <= 0) return AppResult.Failure(DomainError.Validation("Amount must be positive"))
        val entry = WaterEntry(
            id = idGenerator.newId(),
            amountMl = amountMl,
            timestamp = at,
            date = at.toLocalDate(timeProvider.timeZone()),
            source = source,
            note = note,
        )
        return when (val r = repository.addEntry(entry)) {
            is AppResult.Success -> AppResult.Success(entry)
            is AppResult.Failure -> r
        }
    }
}

class EditWaterEntryUseCase(private val repository: WaterEntryRepository) {
    suspend operator fun invoke(entry: WaterEntry): AppResult<Unit> {
        if (entry.amountMl <= 0) return AppResult.Failure(DomainError.Validation("Amount must be positive"))
        return repository.updateEntry(entry)
    }
}

class DeleteWaterEntryUseCase(private val repository: WaterEntryRepository) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.deleteEntry(id)
}

class ObserveEntriesForDateUseCase(private val repository: WaterEntryRepository) {
    operator fun invoke(date: LocalDate): Flow<List<WaterEntry>> = repository.observeEntries(date)
}

/** A quick-add button. [amountMl] is canonical; [label] is unit-aware for display. */
data class QuickAddOption(val amountMl: Int, val label: String)

/** Quick-add presets (100ml/250ml/500ml/1L), labeled for the user's unit system. */
class GetQuickAddOptionsUseCase {
    operator fun invoke(unitSystem: UnitSystem): List<QuickAddOption> =
        PRESETS_ML.map { ml ->
            val label = when (unitSystem) {
                UnitSystem.METRIC -> if (ml >= 1000) "${ml / 1000}L" else "${ml}ml"
                UnitSystem.IMPERIAL -> "${Volume(ml).fluidOunces.toInt()}oz"
            }
            QuickAddOption(ml, label)
        }

    companion object {
        val PRESETS_ML = listOf(100, 250, 500, 1000)
    }
}
