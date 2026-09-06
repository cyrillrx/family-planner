package com.cyrillrx.family.group.domain

import kotlin.time.Instant

sealed interface Invitation {
    val id: InvitationId
    val groupId: GroupId

    /** Never log it. */
    val code: String
    val createdAt: Instant
    val expiresAt: Instant

    companion object {
        /** 128 bits in base64url without padding. Shorter is worth guessing. */
        const val MIN_CODE_LENGTH: Int = 22
    }
}

data class PendingInvitation(
    override val id: InvitationId,
    override val groupId: GroupId,
    override val code: String,
    override val createdAt: Instant,
    override val expiresAt: Instant,
) : Invitation {
    init {
        requireLongEnough(code)
    }
}

data class RedeemedInvitation(
    override val id: InvitationId,
    override val groupId: GroupId,
    override val code: String,
    override val createdAt: Instant,
    override val expiresAt: Instant,
    val redeemedBy: MemberId,
    val redeemedAt: Instant,
) : Invitation {
    init {
        requireLongEnough(code)
    }
}

data class RevokedInvitation(
    override val id: InvitationId,
    override val groupId: GroupId,
    override val code: String,
    override val createdAt: Instant,
    override val expiresAt: Instant,
    val revokedAt: Instant,
) : Invitation {
    init {
        requireLongEnough(code)
    }
}

fun PendingInvitation.hasExpired(now: Instant): Boolean = now >= expiresAt

/** The transition itself cannot fail; [hasExpired] is what decides whether it may run. */
fun PendingInvitation.redeemedBy(member: MemberId, now: Instant) = RedeemedInvitation(
    id = id,
    groupId = groupId,
    code = code,
    createdAt = createdAt,
    expiresAt = expiresAt,
    redeemedBy = member,
    redeemedAt = now,
)

private fun requireLongEnough(code: String) =
    require(code.length >= Invitation.MIN_CODE_LENGTH) {
        "Invitation code is too short to resist guessing"
    }
