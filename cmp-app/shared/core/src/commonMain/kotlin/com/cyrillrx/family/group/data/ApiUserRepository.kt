package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.RegisterUserError
import com.cyrillrx.family.group.domain.User
import com.cyrillrx.family.group.domain.UserId
import com.cyrillrx.family.group.domain.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Keeps [UserApi] and its wire models out of the domain, which only sees [UserRepository]. */
class ApiUserRepository(private val api: UserApi) : UserRepository {

    override fun observeUser(id: UserId): Flow<User?> =
        api.observeUsers(setOf(id.value)).map { answered -> answered.firstOrNull()?.toDomain() }

    override fun observeUsers(ids: Set<UserId>): Flow<List<User>> =
        api.observeUsers(ids.mapTo(mutableSetOf()) { it.value })
            .map { answered -> answered.map(ApiUser::toDomain) }

    override suspend fun register(user: User): Result<User, RegisterUserError> =
        try {
            Result.Success(api.register(user.toRegisterRequest()).toDomain())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (transport: Exception) {
            Result.Failure(RegisterUserError.Unknown)
        }
}
