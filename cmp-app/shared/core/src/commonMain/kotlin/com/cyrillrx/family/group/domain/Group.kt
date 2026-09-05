package com.cyrillrx.family.group.domain

import kotlin.time.Instant

data class Group(
    val id: GroupId,
    val name: String,
    val createdAt: Instant,
)
