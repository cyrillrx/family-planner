package com.cyrillrx.family.group.data

import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.family.group.data.model.ApiRegisterUserRequest
import com.cyrillrx.family.group.data.model.ApiUser

interface UserApi {
    suspend fun register(request: ApiRegisterUserRequest): ApiResponse<ApiUser>
}
