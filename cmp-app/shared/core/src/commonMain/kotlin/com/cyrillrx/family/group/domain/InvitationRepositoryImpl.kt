package com.cyrillrx.family.group.domain

import com.cyrillrx.core.data.model.ApiError
import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.InvitationApi
import com.cyrillrx.family.group.data.model.ApiInvitation
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

    val redeemed = payload?.toRedeemed() ?: return Result.Failure(RedeemInvitationError.Unknown)

    return Result.Success(redeemed)
}

private fun ApiError.toDomain() = when (id) {
    REVOKED -> RedeemInvitationError.Revoked
    ALREADY_REDEEMED -> RedeemInvitationError.AlreadyRedeemed
    EXPIRED -> RedeemInvitationError.Expired
    else -> RedeemInvitationError.Unknown
}

/** Null when the answer is missing anything the domain requires. */
private fun ApiInvitation.toRedeemed(): RedeemedInvitation? = RedeemedInvitation(
    id = InvitationId(id ?: return null),
    groupId = GroupId(groupId ?: return null),
    code = code ?: return null,
    createdAt = Instant.fromEpochMilliseconds(createdAt ?: return null),
    redeemedBy = UserId(redeemedBy ?: return null),
    redeemedAt = Instant.fromEpochMilliseconds(redeemedAt ?: return null),
)

private const val REVOKED = "invitation_revoked"
private const val ALREADY_REDEEMED = "invitation_already_redeemed"
private const val EXPIRED = "invitation_expired"
