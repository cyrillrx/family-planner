package com.cyrillrx.family.group.data.model

import kotlin.time.Instant

data class ApiInvitationResponse(
    val id: String?,
    val groupId: String?,

    /** Never log it. */
    val code: String?,
    val createdAt: Instant?,

    val redeemedBy: String?,
    val redeemedAt: Instant?,
    val expiresAt: Instant?,
    val revokedAt: Instant?,
)
