package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.GroupId
import com.cyrillrx.family.group.domain.Invitation
import com.cyrillrx.family.group.domain.InvitationId
import com.cyrillrx.family.group.domain.PendingInvitation
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.UserId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class InvitationRepositoryTest {

    @Test
    fun `hands a redemption to the api`() = runTest {
        val repository = ApiInvitationRepository(api(pending()))

        val result = repository.redeem(CODE, JOINER)

        assertEquals(JOINER, (result as Result.Success).value.redeemedBy)
    }

    @Test
    fun `hands back the failure the api reports`() = runTest {
        val repository = ApiInvitationRepository(api())

        assertEquals(
            Result.Failure(RedeemInvitationError.Unknown),
            repository.redeem(CODE, JOINER),
        )
    }

    private fun api(vararg invitations: Invitation) =
        RamInvitationApi(clock = FixedClock, initial = invitations.toList())

    private fun pending() = PendingInvitation(
        id = InvitationId("invitation-1"),
        groupId = GroupId("group-1"),
        code = CODE,
        createdAt = Instant.fromEpochMilliseconds(0),
        expiresAt = Instant.fromEpochMilliseconds(10_000),
    )

    private object FixedClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(500)
    }

    private companion object {
        val CODE = "a".repeat(Invitation.MIN_CODE_LENGTH)
        val JOINER = UserId("joiner")
    }
}
