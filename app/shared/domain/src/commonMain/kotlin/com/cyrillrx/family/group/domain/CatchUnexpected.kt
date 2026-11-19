package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import kotlin.coroutines.cancellation.CancellationException

internal inline fun <T> catchUnexpected(call: () -> T): Result<T, Unexpected> = try {
    Result.Success(call())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (defect: Exception) {
    // TODO(#38): record the defect, which nothing reads once it is on screen.
    Result.Failure(Unexpected(defect))
}
