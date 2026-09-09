package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.Invitation
import com.cyrillrx.family.group.domain.Member
import com.cyrillrx.family.group.domain.PendingInvitation
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RedeemedInvitation
import com.cyrillrx.family.group.domain.RevokedInvitation
import com.cyrillrx.family.group.domain.UserId
import com.cyrillrx.family.group.domain.hasExpired
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Plays the owned service of ADR-003, which is the only writer of a group's member list. The target
 * of that write is the whole point, so it is not defaulted.
 */
class RamInvitationApi(
    private val groups: GroupRepository,
    private val clock: Clock = Clock.System,
    initial: List<Invitation> = emptyList(),
) : InvitationApi {

    private val byCode = MutableStateFlow(initial.associateBy { it.code })

    override suspend fun redeem(
        code: String,
        user: UserId,
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
                    val redeemed = invitation.redeemedBy(user, now)
                    // The real service writes both in one transaction. Here the membership goes
                    // first, so a failure cannot burn a single-use code for nothing.
                    groups.addMember(
                        Member(userId = user, groupId = redeemed.groupId, joinedAt = now),
                    )
                    byCode.update { it + (code to redeemed) }
                    Result.Success(redeemed)
                }
        }
    }

    companion object {
        private fun PendingInvitation.redeemedBy(user: UserId, now: Instant) = RedeemedInvitation(
            id = id,
            groupId = groupId,
            code = code,
            createdAt = createdAt,
            redeemedBy = user,
            redeemedAt = now,
        )
    }
}
