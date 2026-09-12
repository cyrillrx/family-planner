package com.cyrillrx.family.group.data

import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.family.group.data.model.ApiRegisterUserRequest
import com.cyrillrx.family.group.data.model.ApiUser
import kotlinx.coroutines.flow.Flow

interface UserApi {
    suspend fun register(request: ApiRegisterUserRequest): ApiResponse<ApiUser>

    fun observeUsers(ids: Set<String>): Flow<List<ApiUser>>
}
