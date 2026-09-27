package com.cyrillrx.family.group.domain

import com.cyrillrx.core.data.model.ApiError
import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.InvitationApi
import com.cyrillrx.family.group.data.model.ApiInvitation
import com.cyrillrx.family.group.domain.InvitationField.CODE
import com.cyrillrx.family.group.domain.InvitationField.CREATED_AT
import com.cyrillrx.family.group.domain.InvitationField.GROUP_ID
import com.cyrillrx.family.group.domain.InvitationField.ID
import com.cyrillrx.family.group.domain.InvitationField.REDEEMED_AT
import com.cyrillrx.family.group.domain.InvitationField.REDEEMED_BY
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.InvitationId
import com.cyrillrx.family.group.domain.model.RedeemedInvitation
import com.cyrillrx.family.group.domain.model.UserId
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
    REVOKED -> RedeemInvitationError.Revoked
    ALREADY_REDEEMED -> RedeemInvitationError.AlreadyRedeemed
    EXPIRED -> RedeemInvitationError.Expired
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

private const val REVOKED = "invitation_revoked"
private const val ALREADY_REDEEMED = "invitation_already_redeemed"
private const val EXPIRED = "invitation_expired"
