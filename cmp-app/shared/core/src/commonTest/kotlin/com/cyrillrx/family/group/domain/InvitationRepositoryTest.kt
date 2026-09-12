package com.cyrillrx.family.group.domain

import com.cyrillrx.core.data.model.ApiError
import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.InvitationApi
import com.cyrillrx.family.group.data.model.ApiInvitation
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.Invitation
import com.cyrillrx.family.group.domain.model.InvitationId
import com.cyrillrx.family.group.domain.model.RedeemedInvitation
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class InvitationRepositoryTest {

    @Test
    fun `hands the code and the user to the api`() = runTest {
        val api = answering(ApiResponse(payload = redeemed()))

        repository(api).redeem(CODE, JOINER)

        assertEquals(CODE, api.lastCode)
        assertEquals("joiner", api.lastUserId)
    }

    @Test
    fun `turns a redeemed answer into the domain`() = runTest {
        val result = repository(answering(ApiResponse(payload = redeemed()))).redeem(CODE, JOINER)

        assertEquals(
            Result.Success(
                RedeemedInvitation(
                    id = InvitationId("invitation-1"),
                    groupId = GroupId("group-1"),
                    code = CODE,
                    createdAt = Instant.fromEpochMilliseconds(0),
                    redeemedBy = JOINER,
                    redeemedAt = Instant.fromEpochMilliseconds(500),
                ),
            ),
            result,
        )
    }

    @Test
    fun `turns a revoked refusal into the domain`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.Revoked),
            repository(refusing("invitation_revoked")).redeem(CODE, JOINER),
        )
    }

    @Test
    fun `turns an already redeemed refusal into the domain`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.AlreadyRedeemed),
            repository(refusing("invitation_already_redeemed")).redeem(CODE, JOINER),
        )
    }

    @Test
    fun `turns an expired refusal into the domain`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.Expired),
            repository(refusing("invitation_expired")).redeem(CODE, JOINER),
        )
    }

    @Test
    fun `falls back to Unknown for a refusal it does not recognise`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.Unknown),
            repository(refusing("something_the_server_added_later")).redeem(CODE, JOINER),
        )
    }

    @Test
    fun `refuses an answer that carries neither a payload nor an error`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.EmptyResponse),
            repository(answering(ApiResponse())).redeem(CODE, JOINER),
        )
    }

    @Test
    fun `names each field an answer left out`() {
        val incomplete = listOf(
            InvitationField.ID to redeemed().copy(id = null),
            InvitationField.GROUP_ID to redeemed().copy(groupId = null),
            InvitationField.CODE to redeemed().copy(code = null),
            InvitationField.CREATED_AT to redeemed().copy(createdAt = null),
            InvitationField.REDEEMED_BY to redeemed().copy(redeemedBy = null),
            InvitationField.REDEEMED_AT to redeemed().copy(redeemedAt = null),
        )

        incomplete.forEach { (missing, payload) ->
            assertEquals(
                Result.Failure(RedeemInvitationError.IncompleteResponse(missing)),
                payload.toRedeemed(),
                "an answer without $missing should name it",
            )
        }
    }

    @Test
    fun `names the first missing field when several are absent`() {
        assertEquals(
            Result.Failure(RedeemInvitationError.IncompleteResponse(InvitationField.ID)),
            ApiInvitation().toRedeemed(),
        )
    }

    @Test
    fun `carries the incomplete answer through to the caller`() = runTest {
        val incomplete = redeemed().copy(redeemedAt = null)

        assertEquals(
            Result.Failure(RedeemInvitationError.IncompleteResponse(InvitationField.REDEEMED_AT)),
            repository(answering(ApiResponse(payload = incomplete))).redeem(CODE, JOINER),
        )
    }

    @Test
    fun `refuses an answer whose code is too short to be one`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.Unknown),
            repository(answering(ApiResponse(payload = redeemed().copy(code = "short"))))
                .redeem(CODE, JOINER),
        )
    }

    @Test
    fun `turns a transport failure into a domain error`() = runTest {
        assertEquals(
            Result.Failure(RedeemInvitationError.Unknown),
            repository(ThrowingInvitationApi(IllegalStateException("no network")))
                .redeem(CODE, JOINER),
        )
    }

    @Test
    fun `lets a cancellation through instead of reporting it as a failure`() = runTest {
        val repository = repository(ThrowingInvitationApi(CancellationException("cancelled")))

        assertFailsWith<CancellationException> { repository.redeem(CODE, JOINER) }
    }

    private fun repository(api: InvitationApi) = InvitationRepositoryImpl(api)

    private fun answering(response: ApiResponse<ApiInvitation>) = RecordingInvitationApi(response)

    private fun refusing(id: String) = answering(ApiResponse(error = ApiError(id = id)))

    private fun redeemed() = ApiInvitation(
        id = "invitation-1",
        groupId = "group-1",
        code = CODE,
        createdAt = 0,
        redeemedBy = "joiner",
        redeemedAt = 500,
    )

    private class RecordingInvitationApi(
        private val response: ApiResponse<ApiInvitation>,
    ) : InvitationApi {
        var lastCode: String? = null
            private set
        var lastUserId: String? = null
            private set

        override suspend fun redeem(code: String, userId: String): ApiResponse<ApiInvitation> {
            lastCode = code
            lastUserId = userId
            return response
        }
    }

    private class ThrowingInvitationApi(private val failure: Throwable) : InvitationApi {
        override suspend fun redeem(code: String, userId: String): ApiResponse<ApiInvitation> =
            throw failure
    }

    private companion object {
        val CODE = "a".repeat(Invitation.MIN_CODE_LENGTH)
        val JOINER = UserId("joiner")
    }
}
