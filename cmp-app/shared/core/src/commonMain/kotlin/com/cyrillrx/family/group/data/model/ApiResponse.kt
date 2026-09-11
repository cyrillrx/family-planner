package com.cyrillrx.family.group.data.model

/**
 * What every endpoint answers: a payload, or a reason it refused. Both are absent when the answer
 * itself is malformed, which the caller has to treat as a failure like any other.
 */
data class ApiResponse<T>(
    val payload: T? = null,
    val error: ApiError? = null,
)

/**
 * [id] is what the caller matches on. The server decides it — nothing is recomputed from the
 * payload, so a rule change does not need a client release.
 */
data class ApiError(
    val id: String,
    val message: String? = null,
)
