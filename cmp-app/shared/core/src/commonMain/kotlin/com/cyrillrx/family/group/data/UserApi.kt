package com.cyrillrx.family.group.data

import kotlinx.coroutines.flow.Flow

/**
 * Registration is idempotent: the same identifier registered twice updates the name rather than
 * creating a second person. The domain never sees this interface, only `UserRepository`.
 */
interface UserApi {
    suspend fun register(request: ApiRegisterUserRequest): ApiUser

    /** Only the identifiers asked for, never the whole collection. */
    fun observeUsers(ids: Set<String>): Flow<List<ApiUser>>
}
