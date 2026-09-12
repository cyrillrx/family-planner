package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    /** Emits null until this user is known locally. */
    fun observeUser(id: UserId): Flow<User?>

    /** Only the users asked for: group isolation is enforced on read, not only on write. */
    fun observeUsers(ids: Set<UserId>): Flow<List<User>>

    /** Idempotent: registering a known identifier updates its name rather than adding a person. */
    suspend fun register(user: User): Result<User, RegisterUserError>
}

sealed interface RegisterUserError : Error {
    data object Unknown : RegisterUserError
    data object EmptyResponse : RegisterUserError
    data class IncompleteResponse(val missing: UserField) : RegisterUserError
}

enum class UserField { ID, DISPLAY_NAME }
