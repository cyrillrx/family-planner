package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class InvitationTest {

    @Test
    fun `redeems the right code before it expires`() {
        val result = invitation().redeem(CODE, JOINER, at(500))

        assertEquals(Result.Success(invitation().copy(redeemedBy = JOINER)), result)
    }

    @Test
    fun `records who redeemed it`() {
        val result = invitation().redeem(CODE, JOINER, at(500))

        assertEquals(JOINER, (result as Result.Success).value.redeemedBy)
    }

    @Test
    fun `leaves the invitation untouched when it fails`() {
        val original = invitation()

        original.redeem("b".repeat(Invitation.MIN_CODE_LENGTH), JOINER, at(500))

        assertEquals(null, original.redeemedBy)
    }

    @Test
    fun `cannot be built with a code short enough to guess`() {
        assertFailsWith<IllegalArgumentException> {
            invitation().copy(code = "a".repeat(Invitation.MIN_CODE_LENGTH - 1))
        }
    }

    @Test
    fun `rejects a code that does not match`() {
        assertFailure(RedeemInvitationError.WrongCode) {
            invitation().redeem("b".repeat(Invitation.MIN_CODE_LENGTH), JOINER, at(500))
        }
    }

    @Test
    fun `rejects a code of the wrong length`() {
        assertFailure(RedeemInvitationError.WrongCode) {
            invitation().redeem(CODE.dropLast(1), JOINER, at(500))
        }
        assertFailure(RedeemInvitationError.WrongCode) {
            invitation().redeem(CODE + "a", JOINER, at(500))
        }
    }

    @Test
    fun `rejects an empty code`() {
        assertFailure(RedeemInvitationError.WrongCode) {
            invitation().redeem("", JOINER, at(500))
        }
    }

    @Test
    fun `rejects the right code once it has expired`() {
        assertFailure(RedeemInvitationError.Expired) {
            invitation().redeem(CODE, JOINER, at(1_000))
        }
    }

    @Test
    fun `treats the expiry instant as already expired`() {
        assertFailure(RedeemInvitationError.Expired) {
            invitation().redeem(CODE, JOINER, at(1_000))
        }
        assertEquals(
            Result.Success(invitation().copy(redeemedBy = JOINER)),
            invitation().redeem(CODE, JOINER, at(999)),
        )
    }

    @Test
    fun `rejects a revoked invitation`() {
        assertFailure(RedeemInvitationError.Revoked) {
            invitation().copy(revokedAt = at(200)).redeem(CODE, JOINER, at(500))
        }
    }

    @Test
    fun `rejects an invitation that has already been redeemed`() {
        assertFailure(RedeemInvitationError.AlreadyRedeemed) {
            invitation().copy(redeemedBy = MemberId("someone")).redeem(CODE, JOINER, at(500))
        }
    }

    @Test
    fun `cannot be redeemed twice`() {
        val redeemed = (invitation().redeem(CODE, JOINER, at(500)) as Result.Success).value

        assertFailure(RedeemInvitationError.AlreadyRedeemed) {
            redeemed.redeem(CODE, MemberId("gatecrasher"), at(600))
        }
    }

    @Test
    fun `reports revocation rather than the code when both are wrong`() {
        assertFailure(RedeemInvitationError.Revoked) {
            invitation().copy(revokedAt = at(200)).redeem("wrong", JOINER, at(500))
        }
    }

    @Test
    fun `reports revocation rather than expiry when both apply`() {
        assertFailure(RedeemInvitationError.Revoked) {
            invitation().copy(revokedAt = at(200)).redeem(CODE, JOINER, at(5_000))
        }
    }

    private fun assertFailure(
        expected: RedeemInvitationError,
        block: () -> Result<Invitation, RedeemInvitationError>,
    ) = assertEquals(Result.Failure(expected), block())

    private fun invitation() = Invitation(
        id = InvitationId("invitation-1"),
        groupId = GroupId("group-1"),
        code = CODE,
        createdAt = at(0),
        expiresAt = at(1_000),
    )

    private fun at(millis: Long) = Instant.fromEpochMilliseconds(millis)

    private companion object {
        val CODE = "a".repeat(Invitation.MIN_CODE_LENGTH)
        val JOINER = MemberId("joiner")
    }
}
