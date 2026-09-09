package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import kotlinx.coroutines.flow.first

/**
 * The first launch asks for a name and one explicit choice (PRD-001). Nothing here runs on its own:
 * the app never creates a group silently, because an invited person who lands in a group of their
 * own cannot be extracted from it.
 */
class Onboarding(
    private val userRepository: UserRepository,
    private val currentUserStore: CurrentUserStore,
    private val ids: IdGenerator = UuidIdGenerator,
) {

    suspend fun register(displayName: String): Result<User, RegisterError> {
        val name = displayName.trim()
        if (name.isEmpty()) return Result.Failure(RegisterError.BlankDisplayName)

        // Reuse the identifier this device already has: registration is idempotent, so an
        // interrupted onboarding resumes instead of leaving a second user behind.
        val id = currentUserStore.observeCurrentUserId().first() ?: ids.newUserId()

        return when (val registered = userRepository.register(User(id = id, displayName = name))) {
            is Result.Success -> registered.also { currentUserStore.setCurrentUserId(it.value.id) }
            is Result.Failure -> Result.Failure(RegisterError.Failed(registered.error))
        }
    }
}

sealed interface RegisterError : Error {
    data object BlankDisplayName : RegisterError
    data class Failed(val cause: RegisterUserError) : RegisterError
}
