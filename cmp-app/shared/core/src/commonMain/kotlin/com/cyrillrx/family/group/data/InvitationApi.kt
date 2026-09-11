package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.data.model.ApiInvitation
import com.cyrillrx.family.group.data.model.ApiResponse

/**
 * Transport only. Redemption itself is the owned service's job (ADR-003) — validating the code,
 * writing the membership, deciding the refusal. Nothing here knows what an invitation means.
 */
interface InvitationApi {
    suspend fun redeem(code: String, userId: String): ApiResponse<ApiInvitation>
}
