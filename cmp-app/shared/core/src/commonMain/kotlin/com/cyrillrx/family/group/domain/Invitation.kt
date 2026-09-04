package com.cyrillrx.family.group.domain

import kotlin.time.Instant

/**
 * A one-time grant to join a group.
 *
 * The invitation is the boundary of the group: members inside it are trusted, whoever holds a code
 * is not yet anyone (ADR-003). Everything below is therefore stated so that the *server* can check
 * it. The client checks the same rules to fail early and say something useful, never as the
 * decision — a check the client alone performs is a check an attacker skips.
 *
 * [code] is a secret with at least [MIN_CODE_LENGTH] characters of encoded randomness, delivered as
 * a share link or QR rather than typed. It must never reach a log, an analytics event or a crash
 * report.
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
        /**
         * 22 characters is what 128 bits comes to in base64url without padding. Below that the code
         * is short enough to be worth guessing, which would make rate limiting load-bearing.
         */
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
 * Whether [invitation] can be redeemed with [presentedCode] at [now].
 *
 * @return null when it can be, and the reason it cannot otherwise.
 */
fun rejectionFor(
    invitation: Invitation,
    presentedCode: String,
    now: Instant,
): InvitationRejection? =
    when {
        // Checked first: an expired or spent invitation must not become an oracle telling a
        // caller whether a code was right.
        invitation.revokedAt != null -> InvitationRejection.REVOKED
        invitation.redeemedBy != null -> InvitationRejection.ALREADY_REDEEMED
        now >= invitation.expiresAt -> InvitationRejection.EXPIRED
        !invitation.code.matchesPresented(presentedCode) -> InvitationRejection.WRONG_CODE
        else -> null
    }

/** Shorthand for [rejectionFor] returning null. */
fun Invitation.acceptsCode(presentedCode: String, now: Instant): Boolean =
    rejectionFor(this, presentedCode, now) == null

/**
 * Compares the whole of both strings rather than stopping at the first difference, so that how
 * long the comparison took says nothing about how much of the code was right.
 */
private fun String.matchesPresented(presented: String): Boolean {
    if (length != presented.length) return false

    var difference = 0
    for (index in indices) {
        difference = difference or (this[index].code xor presented[index].code)
    }
    return difference == 0
}
