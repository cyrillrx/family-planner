package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result

/**
 * Redeeming is a privileged operation: the client asks, the server decides and writes (ADR-003).
 * Nothing here can be upheld by the caller.
 */
interface InvitationGateway {
    suspend fun redeem(code: String, member: MemberId): Result<RedeemedInvitation, RedeemInvitationError>
}

sealed interface RedeemInvitationError : Error {
    data object Unknown : RedeemInvitationError
    data object Revoked : RedeemInvitationError
    data object AlreadyRedeemed : RedeemInvitationError
    data object Expired : RedeemInvitationError
}
