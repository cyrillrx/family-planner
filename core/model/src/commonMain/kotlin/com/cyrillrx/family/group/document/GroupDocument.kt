package com.cyrillrx.family.group.document

import kotlinx.serialization.Serializable

/**
 * [createdAt] is the server's clock, in milliseconds since the epoch.
 *
 * Every field is optional because a document is read back as whichever client version wrote it.
 */
@Serializable
data class GroupDocument(
    val id: String? = null,
    val name: String? = null,
    val createdAt: Long? = null,
)
