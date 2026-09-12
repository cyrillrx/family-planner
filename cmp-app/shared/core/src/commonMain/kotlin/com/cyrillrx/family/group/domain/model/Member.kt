package com.cyrillrx.family.group.domain.model

import kotlin.time.Instant

data class Member(
    val userId: UserId,
    val groupId: GroupId,
    val joinedAt: Instant,
)
