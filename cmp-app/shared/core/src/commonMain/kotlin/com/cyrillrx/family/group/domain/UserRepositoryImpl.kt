package com.cyrillrx.family.group.domain

import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.UserApi
import com.cyrillrx.family.group.data.model.ApiRegisterUserRequest
import com.cyrillrx.family.group.data.model.ApiUser
import com.cyrillrx.family.group.domain.UserField.DISPLAY_NAME
import com.cyrillrx.family.group.domain.UserField.ID
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class UserRepositoryImpl(private val api: UserApi) : UserRepository {

    override fun observeUser(id: UserId): Flow<User?> =
        api.observeUsers(setOf(id.value)).map { answered -> answered.firstOrNull()?.toUserOrNull() }

    override fun observeUsers(ids: Set<UserId>): Flow<List<User>> =
        api.observeUsers(ids.mapTo(mutableSetOf()) { it.value })
            .map { answered -> answered.mapNotNull { it.toUserOrNull() } }

    override suspend fun register(user: User): Result<User, RegisterUserError> =
        try {
            api.register(user.toRegisterRequest()).toDomain()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Result.Failure(RegisterUserError.Unknown)
        }
}

internal fun User.toRegisterRequest() =
    ApiRegisterUserRequest(id = id.value, displayName = displayName)

private fun ApiResponse<ApiUser>.toDomain(): Result<User, RegisterUserError> {
    error?.let { return Result.Failure(RegisterUserError.Unknown) }

    val payload = payload ?: return Result.Failure(RegisterUserError.EmptyResponse)

    return payload.toUser()
}

internal fun ApiUser.toUser(): Result<User, RegisterUserError> = Result.Success(
    User(
        id = UserId(id ?: return missing(ID)),
        displayName = displayName ?: return missing(DISPLAY_NAME),
        authenticatedId = authenticatedId,
    ),
)

/** A user the answer could not describe is dropped from a stream rather than failing all of it. */
private fun ApiUser.toUserOrNull() = (toUser() as? Result.Success)?.value

private fun missing(field: UserField) = Result.Failure(RegisterUserError.IncompleteResponse(field))
