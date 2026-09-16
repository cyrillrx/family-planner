package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.model.User

class Onboarding(
    private val userRepository: UserRepository,
    private val idGenerator: IdGenerator = UuidIdGenerator,
) {

    suspend fun register(displayName: String): Result<User, RegisterUserError> {
        val name = displayName.trim()
        if (name.isEmpty()) return Result.Failure(RegisterUserError.BlankDisplayName)

        val id = userRepository.registeredUserId() ?: idGenerator.newUserId()

        return userRepository.register(User(id = id, displayName = name))
    }
}
