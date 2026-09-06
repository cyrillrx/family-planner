package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import kotlin.time.Instant

data class Invitation(
    val id: InvitationId,
    val groupId: GroupId,
    /** Never log it. */
    val code: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val redeemedBy: MemberId? = null,
    val revokedAt: Instant? = null,
) {
    init {
        require(code.length >= MIN_CODE_LENGTH) { "Invitation code is too short to resist guessing" }
    }

    companion object {
        /** 128 bits in base64url without padding. Shorter is worth guessing. */
        const val MIN_CODE_LENGTH: Int = 22
    }
}

sealed interface RedeemInvitationError : Error {
    data object Revoked : RedeemInvitationError
    data object AlreadyRedeemed : RedeemInvitationError
    data object Expired : RedeemInvitationError
    data object WrongCode : RedeemInvitationError
}

fun Invitation.redeem(
    presentedCode: String,
    member: MemberId,
    now: Instant,
): Result<Invitation, RedeemInvitationError> =
    when {
        // The code is checked last, so a spent invitation cannot answer whether a code was right.
        revokedAt != null -> Result.Failure(RedeemInvitationError.Revoked)
        redeemedBy != null -> Result.Failure(RedeemInvitationError.AlreadyRedeemed)
        now >= expiresAt -> Result.Failure(RedeemInvitationError.Expired)
        !code.matchesPresented(presentedCode) -> Result.Failure(RedeemInvitationError.WrongCode)
        else -> Result.Success(copy(redeemedBy = member))
    }

/** Reads both strings whole, so how long it took says nothing about how much matched. */
private fun String.matchesPresented(presented: String): Boolean {
    if (length != presented.length) return false

    var difference = 0
    for (index in indices) {
        difference = difference or (this[index].code xor presented[index].code)
    }
    return difference == 0
}
