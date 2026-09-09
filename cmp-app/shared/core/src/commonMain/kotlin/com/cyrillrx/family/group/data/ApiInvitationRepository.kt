package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.InvitationRepository
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RedeemedInvitation
import com.cyrillrx.family.group.domain.UserId

/** Keeps [InvitationApi] out of the domain, which only ever sees [InvitationRepository]. */
class ApiInvitationRepository(private val api: InvitationApi) : InvitationRepository {

    override suspend fun redeem(
        code: String,
        user: UserId,
    ): Result<RedeemedInvitation, RedeemInvitationError> = api.redeem(code, user)
}
