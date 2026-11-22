package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.core.domain.flatten
import com.cyrillrx.core.domain.getOrElse
import com.cyrillrx.core.domain.map
import com.cyrillrx.core.domain.onFailure
import com.cyrillrx.family.group.document.UserDocument
import com.cyrillrx.family.group.model.User
import com.cyrillrx.family.group.model.UserId
import com.cyrillrx.logger.Log
import com.cyrillrx.logger.Logger

/**
 * One user per device: the credential this device signed in with addresses it, so registering
 * again renames that user and keeps its identifier, whatever identifier the caller proposed.
 */
class StoreUserRepository(
    private val store: UserStore,
    private val logger: Logger = Log,
) : UserRepository {

    override suspend fun registeredUserId(): Result<Registration, NoAnswer> =
        catchClientDefect { read() }.flatten().onFailure(logger::recordStoreFailure)

    override suspend fun register(user: User): Result<User, RegisterUserError> =
        catchClientDefect { save(user) }.flatten().onFailure(logger::recordStoreFailure)

    private suspend fun read(): Result<Registration, NoAnswer> {
        val credential = store.credential() ?: return Result.Success(Registration.NotRegistered)

        return storedId(credential).map { id -> id?.let(Registration::Registered) ?: Registration.NotRegistered }
    }

    private suspend fun save(user: User): Result<User, RegisterUserError> {
        val held = store.credential()
        val credential = held ?: store.signInAnonymously().getOrElse { return Result.Failure(it) }

        val storedId = held?.let { storedId(it).getOrElse { failure -> return Result.Failure(failure) } }

        val registered = user.copy(id = storedId ?: user.id, authenticatedId = credential)

        return store.save(credential, registered.toDocument()).map { registered }
    }

    private suspend fun storedId(credential: String): Result<UserId?, StoreUnavailable> =
        store.find(credential).map { document -> document?.id?.let(::UserId) }
}

private fun User.toDocument() = UserDocument(
    id = id.value,
    displayName = displayName,
    authenticatedId = authenticatedId,
)
