package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import kotlin.coroutines.cancellation.CancellationException

/** Turns an exception [call] lets through into [Unexpected]: the store reports its own failures as values. */
internal inline fun <T> catchUnexpected(call: () -> T): Result<T, Unexpected> = try {
    Result.Success(call())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (e: Exception) {
    Result.Failure(Unexpected(e))
}
