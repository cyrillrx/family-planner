package com.cyrillrx.family.group.domain

import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.Invitation
import com.cyrillrx.family.group.domain.model.InvitationId
import com.cyrillrx.family.group.domain.model.PendingInvitation
import com.cyrillrx.family.group.domain.model.RedeemedInvitation
import com.cyrillrx.family.group.domain.model.RevokedInvitation
import com.cyrillrx.family.group.domain.model.UserId
import com.cyrillrx.family.group.domain.model.hasExpired
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class InvitationTest {

    @Test
    fun `a pending invitation cannot carry a code short enough to guess`() {
        assertFailsWith<IllegalArgumentException> { pending().copy(code = TOO_SHORT) }
    }

    @Test
    fun `a redeemed invitation cannot carry a code short enough to guess`() {
        val redeemed = RedeemedInvitation(
            id = ID,
            groupId = GROUP,
            code = CODE,
            createdAt = at(0),
            redeemedBy = JOINER,
            redeemedAt = at(500),
        )

        assertFailsWith<IllegalArgumentException> { redeemed.copy(code = TOO_SHORT) }
    }

    @Test
    fun `a revoked invitation cannot carry a code short enough to guess`() {
        val revoked = RevokedInvitation(
            id = ID,
            groupId = GROUP,
            code = CODE,
            createdAt = at(0),
            revokedAt = at(200),
        )

        assertFailsWith<IllegalArgumentException> { revoked.copy(code = TOO_SHORT) }
    }

    @Test
    fun `a pending invitation has not expired before its expiry instant`() {
        assertFalse(pending().hasExpired(at(999)))
    }

    @Test
    fun `a pending invitation treats its expiry instant as already expired`() {
        assertTrue(pending().hasExpired(at(1_000)))
    }

    @Test
    fun `every state exposes the same invitation identity`() {
        val invitations: List<Invitation> = listOf(
            pending(),
            RedeemedInvitation(ID, GROUP, CODE, at(0), JOINER, at(500)),
            RevokedInvitation(ID, GROUP, CODE, at(0), at(200)),
        )

        assertEquals(listOf(ID, ID, ID), invitations.map { it.id })
        assertEquals(listOf(GROUP, GROUP, GROUP), invitations.map { it.groupId })
    }

    private fun pending() = PendingInvitation(
        id = ID,
        groupId = GROUP,
        code = CODE,
        createdAt = at(0),
        expiresAt = at(1_000),
    )

    private fun at(millis: Long) = Instant.fromEpochMilliseconds(millis)

    private companion object {
        val ID = InvitationId("invitation-1")
        val GROUP = GroupId("group-1")
        val CODE = "a".repeat(Invitation.MIN_CODE_LENGTH)
        val TOO_SHORT = "a".repeat(Invitation.MIN_CODE_LENGTH - 1)
        val JOINER = UserId("joiner")
    }
}
