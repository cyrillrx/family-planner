package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.Invitation
import com.cyrillrx.family.group.domain.model.RedeemedInvitation
import com.cyrillrx.family.group.domain.model.UserId
import kotlin.time.Clock

/**
 * One fixed code per outcome the join screen has to render, and [RedeemInvitationError.Unknown]
 * for everything else. Every code is at least [Invitation.MIN_CODE_LENGTH] long, or `Onboarding`
 * would refuse it before this repository ever saw it.
 */
class SampleInvitationRepository(
    private val idGenerator: IdGenerator = UuidIdGenerator,
    private val clock: Clock = Clock.System,
) : InvitationRepository {

    override suspend fun redeem(
        code: String,
        user: UserId,
    ): Result<RedeemedInvitation, RedeemInvitationError> = when (code) {
        ACCEPTED_CODE -> Result.Success(redeemedBy(user))
        REVOKED_CODE -> Result.Failure(RedeemInvitationError.Revoked)
        ALREADY_REDEEMED_CODE -> Result.Failure(RedeemInvitationError.AlreadyRedeemed)
        EXPIRED_CODE -> Result.Failure(RedeemInvitationError.Expired)
        else -> Result.Failure(RedeemInvitationError.Unknown)
    }

    private fun redeemedBy(user: UserId): RedeemedInvitation {
        // Minted on the spot, so both dates come from one reading. A sample has no history.
        val now = clock.now()

        return RedeemedInvitation(
            id = idGenerator.newInvitationId(),
            groupId = GROUP_ID,
            code = ACCEPTED_CODE,
            createdAt = now,
            redeemedBy = user,
            redeemedAt = now,
        )
    }

    companion object {
        /** The group every accepted code leads to. */
        val GROUP_ID = GroupId("sample-group")

        const val ACCEPTED_CODE = "accepted-invitation-01"
        const val REVOKED_CODE = "revoked-invitation-0001"
        const val ALREADY_REDEEMED_CODE = "redeemed-invitation-01"
        const val EXPIRED_CODE = "expired-invitation-0001"
    }
}
