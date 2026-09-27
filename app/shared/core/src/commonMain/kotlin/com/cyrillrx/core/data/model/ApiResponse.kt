package com.cyrillrx.core.data.model

/**
 * What every endpoint answers: a payload, or a reason it refused.
 * Both are absent when the answer itself is malformed, which the caller has to treat as a failure like any other.
 */
data class ApiResponse<T>(
    val payload: T? = null,
    val error: ApiError? = null,
)
