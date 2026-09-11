package com.cyrillrx.family.group.domain.model

import com.cyrillrx.family.group.domain.GroupId
import com.cyrillrx.family.group.domain.InvitationId
import com.cyrillrx.family.group.domain.UserId
import kotlin.time.Instant

sealed interface Invitation {
    val id: InvitationId
    val groupId: GroupId

    /** Never log it. */
    val code: String
    val createdAt: Instant

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
    val expiresAt: Instant,
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
    val redeemedBy: UserId,
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
    val revokedAt: Instant,
) : Invitation {
    init {
        requireLongEnough(code)
    }
}

fun PendingInvitation.hasExpired(now: Instant): Boolean = now >= expiresAt

private fun requireLongEnough(code: String) =
    require(code.length >= Invitation.MIN_CODE_LENGTH) {
        "Invitation code is too short to resist guessing"
    }
