package com.cyrillrx.family.group.data.model

data class ApiInvitation(
    val id: String? = null,
    val groupId: String? = null,
    /** Never log it. */
    val code: String? = null,
    val createdAt: Long? = null,
    val redeemedBy: String? = null,
    val redeemedAt: Long? = null,
)
