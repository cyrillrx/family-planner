package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.ACCEPTED_CODE
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.ALREADY_REDEEMED_CODE
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.EXPIRED_CODE
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.GROUP_ID
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.REVOKED_CODE
import com.cyrillrx.family.group.domain.model.Invitation
import com.cyrillrx.family.group.domain.model.InvitationId
import com.cyrillrx.family.group.domain.model.RedeemedInvitation
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SampleInvitationRepositoryTest {

    @Test
    fun `redeems the accepted code into the sample group`() = runTest {
        val redeemed = SampleInvitationRepository().redeem(ACCEPTED_CODE, ALICE)

        assertEquals(GROUP_ID, redeemed.redeemed().groupId)
    }

    @Test
    fun `records who redeemed it`() = runTest {
        val redeemed = SampleInvitationRepository().redeem(ACCEPTED_CODE, ALICE)

        assertEquals(ALICE, redeemed.redeemed().redeemedBy)
    }

    @Test
    fun `carries the code that was accepted`() = runTest {
        val redeemed = SampleInvitationRepository().redeem(ACCEPTED_CODE, ALICE)

        assertEquals(ACCEPTED_CODE, redeemed.redeemed().code)
    }

    @Test
    fun `reads the clock once so it is never redeemed before it was created`() = runTest {
        val repository = SampleInvitationRepository(clock = AdvancingClock())

        val redeemed = repository.redeem(ACCEPTED_CODE, ALICE).redeemed()

        assertEquals(redeemed.createdAt, redeemed.redeemedAt)
    }

    @Test
    fun `mints one invitation id per redemption`() = runTest {
        val repository = SampleInvitationRepository(idGenerator = CountingIdGenerator())

        val first = repository.redeem(ACCEPTED_CODE, ALICE).redeemed()
        val second = repository.redeem(ACCEPTED_CODE, ALICE).redeemed()

        assertEquals(InvitationId("invitation-1"), first.id)
        assertEquals(InvitationId("invitation-2"), second.id)
    }

    @Test
    fun `refuses a revoked code`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.Revoked),
            SampleInvitationRepository().redeem(REVOKED_CODE, ALICE),
        )
    }

    @Test
    fun `refuses an already redeemed code`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.AlreadyRedeemed),
            SampleInvitationRepository().redeem(ALREADY_REDEEMED_CODE, ALICE),
        )
    }

    @Test
    fun `refuses an expired code`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.Expired),
            SampleInvitationRepository().redeem(EXPIRED_CODE, ALICE),
        )
    }

    @Test
    fun `refuses a code it does not know`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.Unknown),
            SampleInvitationRepository().redeem("unknown-invitation-01", ALICE),
        )
    }

    @Test
    fun `every sample code is long enough to reach the repository`() {
        val codes = listOf(ACCEPTED_CODE, REVOKED_CODE, ALREADY_REDEEMED_CODE, EXPIRED_CODE)

        assertTrue(codes.all { it.length >= Invitation.MIN_CODE_LENGTH })
    }

    private fun Result<RedeemedInvitation, RedeemInvitationError>.redeemed(): RedeemedInvitation =
        (this as Result.Success).value

    /** Hands out a different instant on every reading, so one reading is visible from two. */
    private class AdvancingClock : Clock {
        private var readings = 0

        override fun now(): Instant = NOW + (++readings).seconds
    }

    private class CountingIdGenerator : IdGenerator {
        private var invitations = 0

        override fun newGroupId() = error("The sample repository never mints a group")

        override fun newUserId() = error("The sample repository never mints a user")

        override fun newInvitationId() = InvitationId("invitation-${++invitations}")
    }

    private companion object {
        val ALICE = UserId("alice")
        val NOW: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
