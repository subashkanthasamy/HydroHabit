package com.bose.hydrohabit.data.repository

import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.core.DomainError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** Runs a DB mutation off the calling thread and maps any failure to [DomainError.Storage]. */
internal suspend fun dbWrite(
    dispatcher: CoroutineDispatcher,
    block: () -> Unit,
): AppResult<Unit> = withContext(dispatcher) {
    try {
        block()
        AppResult.Success(Unit)
    } catch (t: Throwable) {
        AppResult.Failure(DomainError.Storage(t.message ?: "Database write failed"))
    }
}
