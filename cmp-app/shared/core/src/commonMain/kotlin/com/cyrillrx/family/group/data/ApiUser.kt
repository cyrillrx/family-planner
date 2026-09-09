package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.domain.User
import com.cyrillrx.family.group.domain.UserId

/**
 * What registration sends. The identifier is ours rather than the store's (ADR-003), so it travels;
 * the credential does not — every call is signed by whoever makes it, and the server reads the
 * identity off the token instead of a body it would have to trust.
 */
data class ApiRegisterUserRequest(
    val id: String,
    val displayName: String,
)

/** What the server answers with, credential included once Phase 2 attaches one. */
data class ApiUser(
    val id: String,
    val displayName: String,
    val authenticatedId: String? = null,
)

fun User.toRegisterRequest() = ApiRegisterUserRequest(id = id.value, displayName = displayName)

fun ApiUser.toDomain() = User(
    id = UserId(id),
    displayName = displayName,
    authenticatedId = authenticatedId,
)
