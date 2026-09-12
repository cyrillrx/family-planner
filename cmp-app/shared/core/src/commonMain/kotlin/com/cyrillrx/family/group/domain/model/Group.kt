package com.cyrillrx.family.group.domain.model

import kotlin.time.Instant

data class Group(
    val id: GroupId,
    val name: String,
    val createdAt: Instant,
)
