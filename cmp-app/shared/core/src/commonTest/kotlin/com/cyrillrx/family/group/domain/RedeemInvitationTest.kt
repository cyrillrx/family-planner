package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class RedeemInvitationTest {

    @Test
    fun `redeems the right code before it expires`() {
        val result = pending().redeem(CODE, JOINER, at(500))

        assertEquals(Result.Success(redeemedAt(500)), result)
    }

    @Test
    fun `records who redeemed it and when`() {
        val redeemed = (pending().redeem(CODE, JOINER, at(500)) as Result.Success).value

        assertEquals(JOINER, redeemed.redeemedBy)
        assertEquals(at(500), redeemed.redeemedAt)
    }

    @Test
    fun `carries the invitation identity over to the redeemed state`() {
        val redeemed = (pending().redeem(CODE, JOINER, at(500)) as Result.Success).value

        assertEquals(pending().id, redeemed.id)
        assertEquals(pending().groupId, redeemed.groupId)
        assertEquals(pending().createdAt, redeemed.createdAt)
        assertEquals(pending().expiresAt, redeemed.expiresAt)
    }

    @Test
    fun `rejects a code that does not match`() {
        assertFailure(RedeemInvitationError.WrongCode) {
            pending().redeem("b".repeat(Invitation.MIN_CODE_LENGTH), JOINER, at(500))
        }
    }

    @Test
    fun `rejects a code of the wrong length`() {
        assertFailure(RedeemInvitationError.WrongCode) {
            pending().redeem(CODE.dropLast(1), JOINER, at(500))
        }
        assertFailure(RedeemInvitationError.WrongCode) {
            pending().redeem(CODE + "a", JOINER, at(500))
        }
    }

    @Test
    fun `rejects an empty code`() {
        assertFailure(RedeemInvitationError.WrongCode) {
            pending().redeem("", JOINER, at(500))
        }
    }

    @Test
    fun `rejects the right code once it has expired`() {
        assertFailure(RedeemInvitationError.Expired) {
            pending().redeem(CODE, JOINER, at(5_000))
        }
    }

    @Test
    fun `treats the expiry instant as already expired`() {
        assertFailure(RedeemInvitationError.Expired) {
            pending().redeem(CODE, JOINER, at(1_000))
        }
        assertEquals(Result.Success(redeemedAt(999)), pending().redeem(CODE, JOINER, at(999)))
    }

    @Test
    fun `reports expiry rather than the code when both are wrong`() {
        assertFailure(RedeemInvitationError.Expired) {
            pending().redeem("wrong", JOINER, at(5_000))
        }
    }

    private fun assertFailure(
        expected: RedeemInvitationError,
        block: () -> Result<RedeemedInvitation, RedeemInvitationError>,
    ) = assertEquals(Result.Failure(expected), block())

    private fun pending() = PendingInvitation(
        id = InvitationId("invitation-1"),
        groupId = GroupId("group-1"),
        code = CODE,
        createdAt = at(0),
        expiresAt = at(1_000),
    )

    private fun redeemedAt(millis: Long) = RedeemedInvitation(
        id = pending().id,
        groupId = pending().groupId,
        code = pending().code,
        createdAt = pending().createdAt,
        expiresAt = pending().expiresAt,
        redeemedBy = JOINER,
        redeemedAt = at(millis),
    )

    private fun at(millis: Long) = Instant.fromEpochMilliseconds(millis)

    private companion object {
        val CODE = "a".repeat(Invitation.MIN_CODE_LENGTH)
        val JOINER = MemberId("joiner")
    }
}
