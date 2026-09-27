package com.cyrillrx.family.group.domain

import com.cyrillrx.core.api.ApiError
import com.cyrillrx.core.api.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.api.ApiInvitation
import com.cyrillrx.family.group.api.INVITATION_ALREADY_REDEEMED
import com.cyrillrx.family.group.api.INVITATION_EXPIRED
import com.cyrillrx.family.group.api.INVITATION_REVOKED
import com.cyrillrx.family.group.data.InvitationApi
import com.cyrillrx.family.group.domain.InvitationField.CODE
import com.cyrillrx.family.group.domain.InvitationField.CREATED_AT
import com.cyrillrx.family.group.domain.InvitationField.GROUP_ID
import com.cyrillrx.family.group.domain.InvitationField.ID
import com.cyrillrx.family.group.domain.InvitationField.REDEEMED_AT
import com.cyrillrx.family.group.domain.InvitationField.REDEEMED_BY
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.InvitationId
import com.cyrillrx.family.group.model.RedeemedInvitation
import com.cyrillrx.family.group.model.UserId
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant

class InvitationRepositoryImpl(private val api: InvitationApi) : InvitationRepository {

    override suspend fun redeem(
        code: String,
        user: UserId,
    ): Result<RedeemedInvitation, RedeemInvitationError> =
        try {
            api.redeem(code, user.value).toDomain()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Result.Failure(RedeemInvitationError.Unknown)
        }
}

private fun ApiResponse<ApiInvitation>.toDomain(): Result<RedeemedInvitation, RedeemInvitationError> {
    error?.let { return Result.Failure(it.toDomain()) }

    val payload = payload ?: return Result.Failure(RedeemInvitationError.EmptyResponse)

    return payload.toRedeemed()
}

private fun ApiError.toDomain() = when (id) {
    INVITATION_REVOKED -> RedeemInvitationError.Revoked
    INVITATION_ALREADY_REDEEMED -> RedeemInvitationError.AlreadyRedeemed
    INVITATION_EXPIRED -> RedeemInvitationError.Expired
    else -> RedeemInvitationError.Unknown
}

internal fun ApiInvitation.toRedeemed(): Result<RedeemedInvitation, RedeemInvitationError> = Result.Success(
    RedeemedInvitation(
        id = InvitationId(id ?: return missing(ID)),
        groupId = GroupId(groupId ?: return missing(GROUP_ID)),
        code = code ?: return missing(CODE),
        createdAt = Instant.fromEpochMilliseconds(createdAt ?: return missing(CREATED_AT)),
        redeemedBy = UserId(redeemedBy ?: return missing(REDEEMED_BY)),
        redeemedAt = Instant.fromEpochMilliseconds(redeemedAt ?: return missing(REDEEMED_AT)),
    ),
)

private fun missing(field: InvitationField) = Result.Failure(RedeemInvitationError.IncompleteResponse(field))
