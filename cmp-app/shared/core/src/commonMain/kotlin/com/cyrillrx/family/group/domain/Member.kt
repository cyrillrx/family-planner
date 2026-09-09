package com.cyrillrx.family.group.domain

import kotlin.time.Instant

/**
 * A user's place in a group, and nothing about the user. The pair ([groupId], [userId]) identifies
 * it — the same user cannot belong to the same group twice — so it carries no identifier of its own.
 *
 * The display name is on [User]. Reading a group's members therefore joins the two.
 */
data class Member(
    val userId: UserId,
    val groupId: GroupId,
    val joinedAt: Instant,
)
