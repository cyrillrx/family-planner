package com.cyrillrx.family.group.domain

import kotlin.time.Instant

data class Member(
    val id: UserId,
    val displayName: String,
    val joinedAt: Instant,
    val authenticatedId: String? = null,
)
