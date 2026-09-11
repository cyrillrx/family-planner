package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.InvitationApi

class InvitationRepositoryImpl(private val api: InvitationApi) : InvitationRepository {

    override suspend fun redeem(
        code: String,
        user: UserId,
    ): Result<RedeemedInvitation, RedeemInvitationError> = api.redeem(code, user)
}
