package com.cyrillrx.family.group.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class InvitationTest {

    @Test
    fun `accepts the right code before it expires`() {
        assertNull(rejectionFor(invitation(), CODE, at(500)))
        assertTrue(invitation().acceptsCode(CODE, at(500)))
    }

    @Test
    fun `rejects a code that does not match`() {
        assertEquals(
            InvitationRejection.WRONG_CODE,
            rejectionFor(invitation(), "b".repeat(Invitation.MIN_CODE_LENGTH), at(500)),
        )
    }

    @Test
    fun `rejects a code of the wrong length`() {
        assertFalse(invitation().acceptsCode(CODE.dropLast(1), at(500)))
        assertFalse(invitation().acceptsCode(CODE + "a", at(500)))
    }

    @Test
    fun `rejects an empty code`() {
        assertFalse(invitation().acceptsCode("", at(500)))
    }

    @Test
    fun `rejects the right code once it has expired`() {
        assertEquals(InvitationRejection.EXPIRED, rejectionFor(invitation(), CODE, at(1_000)))
    }

    @Test
    fun `treats the expiry instant as already expired`() {
        // The invitation expires at 1000; accepting exactly at 1000 would make the window one
        // millisecond longer than it reads.
        assertFalse(invitation().acceptsCode(CODE, at(1_000)))
        assertTrue(invitation().acceptsCode(CODE, at(999)))
    }

    @Test
    fun `rejects a revoked invitation`() {
        val revoked = invitation().copy(revokedAt = at(200))

        assertEquals(InvitationRejection.REVOKED, rejectionFor(revoked, CODE, at(500)))
    }

    @Test
    fun `rejects an invitation that has already been redeemed`() {
        val spent = invitation().copy(redeemedBy = MemberId("member-1"))

        assertEquals(InvitationRejection.ALREADY_REDEEMED, rejectionFor(spent, CODE, at(500)))
    }

    @Test
    fun `cannot be redeemed twice`() {
        val fresh = invitation()
        assertTrue(fresh.acceptsCode(CODE, at(500)))

        val spent = fresh.copy(redeemedBy = MemberId("member-1"))
        assertFalse(spent.acceptsCode(CODE, at(500)))
    }

    @Test
    fun `reports revocation rather than the code when both are wrong`() {
        // A spent or revoked invitation must not tell a caller whether the code was right.
        val revoked = invitation().copy(revokedAt = at(200))

        assertEquals(InvitationRejection.REVOKED, rejectionFor(revoked, "wrong", at(500)))
    }

    @Test
    fun `reports revocation rather than expiry when both apply`() {
        val revoked = invitation().copy(revokedAt = at(200))

        assertEquals(InvitationRejection.REVOKED, rejectionFor(revoked, CODE, at(5_000)))
    }

    private fun invitation() = Invitation(
        id = InvitationId("invitation-1"),
        groupId = GroupId("group-1"),
        code = CODE,
        createdAt = at(0),
        expiresAt = at(1_000),
    )

    private fun at(millis: Long) = Instant.fromEpochMilliseconds(millis)

    private companion object {
        const val CODE = "aaaaaaaaaaaaaaaaaaaaaa"
    }
}
