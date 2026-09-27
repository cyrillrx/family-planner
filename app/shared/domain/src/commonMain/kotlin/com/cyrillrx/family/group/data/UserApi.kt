package com.cyrillrx.family.group.data

import com.cyrillrx.core.api.ApiResponse
import com.cyrillrx.family.group.api.ApiRegisterUserRequest
import com.cyrillrx.family.group.api.ApiUser

interface UserApi {
    suspend fun register(request: ApiRegisterUserRequest): ApiResponse<ApiUser>
}
