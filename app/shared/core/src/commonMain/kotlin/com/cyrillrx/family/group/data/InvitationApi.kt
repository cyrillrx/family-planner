package com.cyrillrx.family.group.data

import com.cyrillrx.core.api.ApiResponse
import com.cyrillrx.family.group.api.ApiInvitation

interface InvitationApi {
    suspend fun redeem(code: String, userId: String): ApiResponse<ApiInvitation>
}
