package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.model.RedeemedInvitation
import com.cyrillrx.family.group.domain.model.UserId

interface InvitationRepository {
    suspend fun redeem(code: String, user: UserId): Result<RedeemedInvitation, RedeemInvitationError>
}

sealed interface RedeemInvitationError : Error {
    data object Unknown : RedeemInvitationError
    data object Revoked : RedeemInvitationError
    data object AlreadyRedeemed : RedeemInvitationError
    data object Expired : RedeemInvitationError
    data object EmptyResponse : RedeemInvitationError
    data class IncompleteResponse(val missing: InvitationField) : RedeemInvitationError
}

enum class InvitationField { ID, GROUP_ID, CODE, CREATED_AT, REDEEMED_BY, REDEEMED_AT }
