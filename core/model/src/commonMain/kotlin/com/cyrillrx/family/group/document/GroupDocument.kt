package com.cyrillrx.family.group.document

import kotlinx.serialization.Serializable

@Serializable
data class GroupDocument(
    val id: String? = null,
    val name: String? = null,
    val createdAt: Long? = null,
)
