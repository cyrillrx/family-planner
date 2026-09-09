package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.GroupId
import com.cyrillrx.family.group.domain.Invitation
import com.cyrillrx.family.group.domain.InvitationId
import com.cyrillrx.family.group.domain.PendingInvitation
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RedeemedInvitation
import com.cyrillrx.family.group.domain.RevokedInvitation
import com.cyrillrx.family.group.domain.UserId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class RamInvitationApiTest {

    @Test
    fun `rejects a code it does not know`() = runTest {
        val api = api(pending())

        assertEquals(
            Result.Failure(RedeemInvitationError.Unknown),
            api.redeem("b".repeat(Invitation.MIN_CODE_LENGTH), JOINER),
        )
    }

    @Test
    fun `knows no code when it is built empty`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.Unknown),
            RamInvitationApi().redeem(CODE, JOINER),
        )
    }

    @Test
    fun `redeems a pending invitation`() = runTest {
        val api = api(pending())

        val result = api.redeem(CODE, JOINER)

        val redeemed = (result as Result.Success).value
        assertEquals(JOINER, redeemed.redeemedBy)
        assertEquals(NOW, redeemed.redeemedAt)
        assertEquals(pending().id, redeemed.id)
    }

    @Test
    fun `refuses a second redemption of the same code`() = runTest {
        val api = api(pending())
        api.redeem(CODE, JOINER)

        assertEquals(
            Result.Failure(RedeemInvitationError.AlreadyRedeemed),
            api.redeem(CODE, UserId("gatecrasher")),
        )
    }

    @Test
    fun `rejects an invitation that was already redeemed before`() = runTest {
        val redeemed = RedeemedInvitation(
            id = InvitationId("invitation-1"),
            groupId = GroupId("group-1"),
            code = CODE,
            createdAt = at(0),
            redeemedBy = JOINER,
            redeemedAt = NOW,
        )

        assertEquals(
            Result.Failure(RedeemInvitationError.AlreadyRedeemed),
            api(redeemed).redeem(CODE, UserId("gatecrasher")),
        )
    }

    @Test
    fun `rejects a revoked invitation`() = runTest {
        val revoked = RevokedInvitation(
            id = InvitationId("invitation-1"),
            groupId = GroupId("group-1"),
            code = CODE,
            createdAt = at(0),
            revokedAt = at(100),
        )

        assertEquals(
            Result.Failure(RedeemInvitationError.Revoked),
            api(revoked).redeem(CODE, JOINER),
        )
    }

    @Test
    fun `rejects an expired invitation`() = runTest {
        val api = api(pending().copy(expiresAt = at(100)))

        assertEquals(
            Result.Failure(RedeemInvitationError.Expired),
            api.redeem(CODE, JOINER),
        )
    }

    @Test
    fun `treats the expiry instant as already expired`() = runTest {
        val api = api(pending().copy(expiresAt = NOW))

        assertEquals(
            Result.Failure(RedeemInvitationError.Expired),
            api.redeem(CODE, JOINER),
        )
    }

    private fun api(vararg invitations: Invitation) =
        RamInvitationApi(clock = FixedClock, initial = invitations.toList())

    private fun pending() = PendingInvitation(
        id = InvitationId("invitation-1"),
        groupId = GroupId("group-1"),
        code = CODE,
        createdAt = at(0),
        expiresAt = at(10_000),
    )

    private object FixedClock : Clock {
        override fun now(): Instant = NOW
    }

    private companion object {
        val CODE = "a".repeat(Invitation.MIN_CODE_LENGTH)
        val JOINER = UserId("joiner")
        val NOW: Instant = Instant.fromEpochMilliseconds(500)

        fun at(millis: Long): Instant = Instant.fromEpochMilliseconds(millis)
    }
}
