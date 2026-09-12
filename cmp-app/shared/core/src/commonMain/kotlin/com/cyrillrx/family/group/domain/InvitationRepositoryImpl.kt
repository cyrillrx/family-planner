package com.cyrillrx.family.group.domain

import com.cyrillrx.core.data.model.ApiError
import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.core.domain.Error
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

/** Which field an answer left out. A malformed answer is a server bug, so it never reaches the UI. */
internal enum class InvitationField { ID, GROUP_ID, CODE, CREATED_AT, REDEEMED_BY, REDEEMED_AT }

internal data class MalformedInvitationError(val missing: InvitationField) : Error

private fun ApiResponse<ApiInvitation>.toDomain(): Result<RedeemedInvitation, RedeemInvitationError> {
    error?.let { return Result.Failure(it.toDomain()) }

    val payload = payload ?: return Result.Failure(RedeemInvitationError.Unknown)

    return when (val redeemed = payload.toRedeemed()) {
        is Result.Success -> redeemed
        is Result.Failure -> Result.Failure(RedeemInvitationError.Unknown)
    }
}

private fun ApiError.toDomain() = when (id) {
    REVOKED -> RedeemInvitationError.Revoked
    ALREADY_REDEEMED -> RedeemInvitationError.AlreadyRedeemed
    EXPIRED -> RedeemInvitationError.Expired
    else -> RedeemInvitationError.Unknown
}

internal fun ApiInvitation.toRedeemed(): Result<RedeemedInvitation, MalformedInvitationError> {
    val id = id ?: return missing(ID)
    val groupId = groupId ?: return missing(GROUP_ID)
    val code = code ?: return missing(CODE)
    val createdAt = createdAt ?: return missing(CREATED_AT)
    val redeemedBy = redeemedBy ?: return missing(REDEEMED_BY)
    val redeemedAt = redeemedAt ?: return missing(REDEEMED_AT)

    return Result.Success(
        RedeemedInvitation(
            id = InvitationId(id),
            groupId = GroupId(groupId),
            code = code,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            redeemedBy = UserId(redeemedBy),
            redeemedAt = Instant.fromEpochMilliseconds(redeemedAt),
        ),
    )
}

private fun missing(field: InvitationField) = Result.Failure(MalformedInvitationError(field))

private const val REVOKED = "invitation_revoked"
private const val ALREADY_REDEEMED = "invitation_already_redeemed"
private const val EXPIRED = "invitation_expired"
