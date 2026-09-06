package com.cyrillrx.family.group.domain

import kotlin.time.Instant

data class Member(
    val id: MemberId,
    val displayName: String,
    val joinedAt: Instant,
    val authenticatedId: String? = null,
)
