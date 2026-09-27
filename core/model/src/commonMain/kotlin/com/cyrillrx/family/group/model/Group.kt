package com.cyrillrx.family.group.model

import kotlin.time.Instant

data class Group(
    val id: GroupId,
    val name: String,
    val createdAt: Instant,
)
