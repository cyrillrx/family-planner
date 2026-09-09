package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RedeemedInvitation
import com.cyrillrx.family.group.domain.UserId

/**
 * Request and response models arrive with the first real transport. Until then the api speaks
 * domain types, which the data layer may see; the domain never sees this interface.
 */
interface InvitationApi {
    suspend fun redeem(code: String, user: UserId): Result<RedeemedInvitation, RedeemInvitationError>
}
