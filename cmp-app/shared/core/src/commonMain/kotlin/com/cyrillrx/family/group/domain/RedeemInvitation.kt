package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import kotlin.time.Instant

sealed interface RedeemInvitationError : Error {
    data object Expired : RedeemInvitationError
    data object WrongCode : RedeemInvitationError
}

fun PendingInvitation.redeem(
    presentedCode: String,
    member: MemberId,
    now: Instant,
): Result<RedeemedInvitation, RedeemInvitationError> =
    when {
        // Expiry is checked first, so an expired invitation cannot answer whether a code was right.
        now >= expiresAt -> Result.Failure(RedeemInvitationError.Expired)
        !code.matchesPresented(presentedCode) -> Result.Failure(RedeemInvitationError.WrongCode)
        else -> Result.Success(
            RedeemedInvitation(
                id = id,
                groupId = groupId,
                code = code,
                createdAt = createdAt,
                expiresAt = expiresAt,
                redeemedBy = member,
                redeemedAt = now,
            ),
        )
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
