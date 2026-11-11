package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import kotlin.coroutines.cancellation.CancellationException

/**
 * The store could not be reached, or refused the call. Unlike an answer of "nothing", it tells
 * nothing about what the store holds.
 */
data object StoreUnavailable : GroupReadError, CreateGroupWriteError

internal suspend fun <T> reachStore(call: suspend () -> T): Result<T, StoreUnavailable> = try {
    Result.Success(call())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (e: Exception) {
    // TODO(#94): catch only the failures of the store, not a programming error.
    Result.Failure(StoreUnavailable)
}
