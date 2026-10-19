package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.UserStore
import com.cyrillrx.family.group.document.UserDocument
import com.cyrillrx.family.group.model.User
import com.cyrillrx.family.group.model.UserId
import kotlin.coroutines.cancellation.CancellationException

/**
 * One user per device: the credential this device signed in with addresses it, so registering
 * again renames that user and keeps its identifier, whatever identifier the caller proposed.
 */
class StoreUserRepository(private val store: UserStore) : UserRepository {

    /** Null when the store cannot tell: [register] keeps the identifier either way. */
    override suspend fun registeredUserId(): UserId? = try {
        store.credential()?.let { storedId(it) }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (e: Exception) {
        null
    }

    override suspend fun register(user: User): Result<User, RegisterUserError> = try {
        Result.Success(save(user))
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (e: Exception) {
        Result.Failure(RegisterUserError.Unknown)
    }

    private suspend fun save(user: User): User {
        val credential = store.credential() ?: store.signInAnonymously()
        val registered = user.copy(id = storedId(credential) ?: user.id, authenticatedId = credential)

        store.save(credential, registered.toDocument())

        return registered
    }

    private suspend fun storedId(credential: String): UserId? = store.find(credential)?.id?.let(::UserId)
}

private fun User.toDocument() = UserDocument(
    id = id.value,
    displayName = displayName,
    authenticatedId = authenticatedId,
)
