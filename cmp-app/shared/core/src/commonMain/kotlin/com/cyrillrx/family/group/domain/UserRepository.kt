package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId

interface UserRepository {

    suspend fun registeredUserId(): UserId?

    suspend fun register(user: User): Result<User, RegisterUserError>
}

sealed interface RegisterUserError : Error {
    data object Unknown : RegisterUserError
    data object EmptyResponse : RegisterUserError
    data class IncompleteResponse(val missing: UserField) : RegisterUserError
}

enum class UserField { ID, DISPLAY_NAME }
