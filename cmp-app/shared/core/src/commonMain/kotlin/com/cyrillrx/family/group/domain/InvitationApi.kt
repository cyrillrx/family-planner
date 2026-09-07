package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result

interface InvitationApi {
    suspend fun redeem(code: String, member: MemberId): Result<RedeemedInvitation, RedeemInvitationError>
}

sealed interface RedeemInvitationError : Error {
    data object Unknown : RedeemInvitationError
    data object Revoked : RedeemInvitationError
    data object AlreadyRedeemed : RedeemInvitationError
    data object Expired : RedeemInvitationError
}
