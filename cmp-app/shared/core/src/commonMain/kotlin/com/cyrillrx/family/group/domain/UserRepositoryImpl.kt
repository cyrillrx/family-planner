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
import kotlin.coroutines.cancellation.CancellationException

class UserRepositoryImpl(
    private val api: UserApi,
    private var current: UserId? = null,
) : UserRepository {

    override suspend fun registeredUserId(): UserId? = current

    override suspend fun register(user: User): Result<User, RegisterUserError> {
        val registered = try {
            api.register(user.toRegisterRequest()).toDomain()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Result.Failure(RegisterUserError.Unknown)
        }

        if (registered is Result.Success) current = registered.value.id

        return registered
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
        displayName = displayName?.takeIf { it.isNotBlank() } ?: return missing(DISPLAY_NAME),
        authenticatedId = authenticatedId,
    ),
)

private fun missing(field: UserField) = Result.Failure(RegisterUserError.IncompleteResponse(field))
