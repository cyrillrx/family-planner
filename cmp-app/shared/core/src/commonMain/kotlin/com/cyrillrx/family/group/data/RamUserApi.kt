package com.cyrillrx.family.group.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class RamUserApi(initial: List<ApiUser> = emptyList()) : UserApi {

    private val byId = MutableStateFlow(initial.associateBy { it.id })

    override suspend fun register(request: ApiRegisterUserRequest): ApiUser {
        val registered = ApiUser(id = request.id, displayName = request.displayName)

        byId.update { it + (registered.id to registered) }

        return registered
    }

    override fun observeUsers(ids: Set<String>): Flow<List<ApiUser>> =
        byId.map { known -> ids.mapNotNull(known::get) }
}
