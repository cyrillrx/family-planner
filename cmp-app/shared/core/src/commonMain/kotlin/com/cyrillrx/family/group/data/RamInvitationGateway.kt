package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.Invitation
import com.cyrillrx.family.group.domain.InvitationGateway
import com.cyrillrx.family.group.domain.MemberId
import com.cyrillrx.family.group.domain.PendingInvitation
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RedeemedInvitation
import com.cyrillrx.family.group.domain.RevokedInvitation
import com.cyrillrx.family.group.domain.hasExpired
import com.cyrillrx.family.group.domain.redeemedBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

class RamInvitationGateway(
    private val clock: Clock = Clock.System,
    initial: List<Invitation> = emptyList(),
) : InvitationGateway {

    private val byCode = MutableStateFlow(initial.associateBy { it.code })

    override suspend fun redeem(
        code: String,
        member: MemberId,
    ): Result<RedeemedInvitation, RedeemInvitationError> {
        val now = clock.now()

        return when (val invitation = byCode.value[code]) {
            null -> Result.Failure(RedeemInvitationError.Unknown)
            is RevokedInvitation -> Result.Failure(RedeemInvitationError.Revoked)
            is RedeemedInvitation -> Result.Failure(RedeemInvitationError.AlreadyRedeemed)
            is PendingInvitation ->
                if (invitation.hasExpired(now)) {
                    Result.Failure(RedeemInvitationError.Expired)
                } else {
                    val redeemed = invitation.redeemedBy(member, now)
                    byCode.update { it + (code to redeemed) }
                    Result.Success(redeemed)
                }
        }
    }
}
