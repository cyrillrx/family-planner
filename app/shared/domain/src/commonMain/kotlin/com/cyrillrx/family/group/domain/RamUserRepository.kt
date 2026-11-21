package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.model.User
import kotlinx.coroutines.flow.MutableStateFlow

class RamUserRepository : UserRepository {

    private val registered = MutableStateFlow<User?>(null)

    override suspend fun registeredUserId(): Result<Registration, NoAnswer> =
        Result.Success(registered.value?.id?.let(Registration::Registered) ?: Registration.NotRegistered)

    override suspend fun register(user: User): Result<User, RegisterUserError> {
        registered.value = user

        return Result.Success(user)
    }
}
