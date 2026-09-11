package com.cyrillrx.family.group.data.model

/**
 * An invitation as it travels. Every field is nullable because the wire says so, not because the
 * model allows it: what the domain requires is checked when the response is translated.
 */
data class ApiInvitation(
    val id: String? = null,
    val groupId: String? = null,
    /** Never log it. */
    val code: String? = null,
    val createdAt: Long? = null,
    val redeemedBy: String? = null,
    val redeemedAt: Long? = null,
)
