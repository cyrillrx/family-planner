package com.cyrillrx.family.group.document

import kotlinx.serialization.Serializable

/**
 * A membership as the client reads it, under its group and addressed by the [credential] of the
 * member, which is what the security rules authorize on (ADR-008). [joinedAt] is the server's
 * clock, in milliseconds since the epoch.
 *
 * Every field is optional because a document is read back as whichever client version wrote it.
 */
@Serializable
data class MemberDocument(
    val userId: String? = null,
    val groupId: String? = null,
    val credential: String? = null,
    val joinedAt: Long? = null,
)
