package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import kotlin.coroutines.cancellation.CancellationException

internal inline fun <T> catchClientDefect(call: () -> T): Result<T, ClientDefect> = try {
    Result.Success(call())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (defect: Exception) {
    // TODO(#38): record the defect.
    Result.Failure(ClientDefect(defect))
}
