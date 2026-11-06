package com.cyrillrx.family.group.document

import kotlinx.serialization.Serializable

@Serializable
data class MemberDocument(
    val userId: String? = null,
    val groupId: String? = null,
    val credential: String? = null,
    val joinedAt: Long? = null,
)
