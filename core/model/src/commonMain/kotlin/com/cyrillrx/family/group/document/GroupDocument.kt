package com.cyrillrx.family.group.document

import kotlinx.serialization.Serializable

/**
 * A group as it is stored, addressed by its identifier. [createdAt] is the server's clock, in
 * milliseconds since the epoch.
 *
 * Every field is optional for the reason [UserDocument] gives.
 */
@Serializable
data class GroupDocument(
    val id: String? = null,
    val name: String? = null,
    val createdAt: Long? = null,
)
