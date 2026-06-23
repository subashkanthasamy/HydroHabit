package com.bose.hydrohabit.core

/**
 * A lightweight functional result type used across the domain/data boundary.
 *
 * Reads are exposed as [kotlinx.coroutines.flow.Flow]; writes and one-shot operations return
 * [AppResult] so callers handle failures explicitly without exceptions crossing layers.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: DomainError) : AppResult<Nothing>

    val isSuccess: Boolean get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.value

    fun errorOrNull(): DomainError? = (this as? Failure)?.error
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(value)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (DomainError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}

fun <T> T.asSuccess(): AppResult<T> = AppResult.Success(this)
fun DomainError.asFailure(): AppResult<Nothing> = AppResult.Failure(this)

/** Typed, platform-agnostic errors. Keep them coarse; UI maps them to messages. */
sealed class DomainError(val message: String) {
    data class Validation(val reason: String) : DomainError(reason)
    data object NotFound : DomainError("Requested data was not found")
    data class Storage(val cause: String) : DomainError(cause)
    data class Unexpected(val cause: String) : DomainError(cause)
}
