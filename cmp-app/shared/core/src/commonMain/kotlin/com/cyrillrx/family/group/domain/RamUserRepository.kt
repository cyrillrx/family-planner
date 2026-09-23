package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A store, not a service: registration never fails here. The refusals belong to
 * [UserRepositoryImpl], which has an api to fail.
 */
class RamUserRepository : UserRepository {

    /** A thread-safe box for the same reason as [UserRepositoryImpl]'s: writers and readers differ. */
    private val registered = MutableStateFlow<User?>(null)

    override suspend fun registeredUserId(): UserId? = registered.value?.id

    override suspend fun register(user: User): Result<User, RegisterUserError> {
        registered.value = user

        return Result.Success(user)
    }
}
