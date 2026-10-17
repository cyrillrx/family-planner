package com.cyrillrx.family.group.document

import kotlinx.serialization.Serializable

/**
 * A user as it is stored, addressed by the credential that authenticates it.
 *
 * Every field is optional because a document is read back as it was written, by whichever client
 * version wrote it: what a missing field means is the reader's decision, not the decoder's.
 */
@Serializable
data class UserDocument(
    val id: String? = null,
    val displayName: String? = null,
    val authenticatedId: String? = null,
)
