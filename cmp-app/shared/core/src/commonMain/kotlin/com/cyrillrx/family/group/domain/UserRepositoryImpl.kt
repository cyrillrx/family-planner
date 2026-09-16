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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.cancellation.CancellationException

class UserRepositoryImpl internal constructor(
    private val api: UserApi,
    initial: UserId?,
) : UserRepository {

    constructor(api: UserApi) : this(api, null)

    /**
     * A thread-safe box rather than a stream: nobody but [register] writes it, and it resumes
     * on whatever dispatcher the api used, so the write needs to be visible to the next reader.
     */
    private val current = MutableStateFlow(initial)

    /**
     * Forgotten when the process dies. Persisting the identifier is infrastructure and belongs
     * to the Firebase implementation, per ADR-004.
     */
    override suspend fun registeredUserId(): UserId? = current.value

    override suspend fun register(user: User): Result<User, RegisterUserError> {
        val registered = try {
            api.register(user.toRegisterRequest()).toDomain()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Result.Failure(RegisterUserError.Unknown)
        }

        if (registered is Result.Success) current.value = registered.value.id

        return registered
    }
}

internal fun User.toRegisterRequest() =
    ApiRegisterUserRequest(id = id.value, displayName = displayName)

private fun ApiResponse<ApiUser>.toDomain(): Result<User, RegisterUserError> {
    // The id is deliberately ignored: `server/` names the refusals, and the mapping lands with it.
    if (error != null) return Result.Failure(RegisterUserError.Unknown)

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
