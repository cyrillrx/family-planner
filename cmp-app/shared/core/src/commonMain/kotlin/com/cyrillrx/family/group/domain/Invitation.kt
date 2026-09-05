package com.cyrillrx.family.group.domain

import kotlin.time.Instant

/**
 * A one-time grant to join a group.
 *
 * @param code a secret of at least [MIN_CODE_LENGTH] characters. Never log it.
 */
data class Invitation(
    val id: InvitationId,
    val groupId: GroupId,
    val code: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val redeemedBy: MemberId? = null,
    val revokedAt: Instant? = null,
) {
    companion object {
        /** 128 bits in base64url without padding. Shorter is worth guessing. */
        const val MIN_CODE_LENGTH: Int = 22
    }
}

/** Why an invitation cannot be redeemed. */
enum class InvitationRejection {
    EXPIRED,
    REVOKED,
    ALREADY_REDEEMED,
    WRONG_CODE,
}

/**
 * Whether [invitation] can be redeemed with [presentedCode] at [now]. The result is advisory: only
 * a check the caller cannot skip decides admission to a group.
 *
 * @return null when it can be redeemed, the reason it cannot otherwise.
 */
fun rejectionFor(
    invitation: Invitation,
    presentedCode: String,
    now: Instant,
): InvitationRejection? =
    when {
        // The code is checked last, so a spent invitation cannot answer whether a code was right.
        invitation.revokedAt != null -> InvitationRejection.REVOKED
        invitation.redeemedBy != null -> InvitationRejection.ALREADY_REDEEMED
        now >= invitation.expiresAt -> InvitationRejection.EXPIRED
        !invitation.code.matchesPresented(presentedCode) -> InvitationRejection.WRONG_CODE
        else -> null
    }

/** Shorthand for [rejectionFor] returning null. */
fun Invitation.acceptsCode(presentedCode: String, now: Instant): Boolean =
    rejectionFor(this, presentedCode, now) == null

/** Reads both strings whole, so how long it took says nothing about how much matched. */
private fun String.matchesPresented(presented: String): Boolean {
    if (length != presented.length) return false

    var difference = 0
    for (index in indices) {
        difference = difference or (this[index].code xor presented[index].code)
    }
    return difference == 0
}
