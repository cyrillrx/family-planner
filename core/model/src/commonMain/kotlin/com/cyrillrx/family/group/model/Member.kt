package com.cyrillrx.family.group.model

import kotlin.time.Instant

data class Member(
    val userId: UserId,
    val groupId: GroupId,
    val joinedAt: Instant,
)
