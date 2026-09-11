package com.cyrillrx.family.group.data

import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.family.group.data.model.ApiInvitation

interface InvitationApi {
    suspend fun redeem(code: String, userId: String): ApiResponse<ApiInvitation>
}
