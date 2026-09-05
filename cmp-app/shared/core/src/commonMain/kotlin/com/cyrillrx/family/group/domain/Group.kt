package com.cyrillrx.family.group.domain

import kotlin.time.Instant

/**
 * The unit that owns every piece of shared data.
 *
 * @param name generated, and displayed nowhere until a member can belong to more than one group.
 */
data class Group(
    val id: GroupId,
    val name: String,
    val createdAt: Instant,
)

/**
 * A person in a group.
 *
 * @param displayName the only thing about a member the others see.
 * @param credentialId whatever the authentication provider uses to recognise them, or null while
 *   they are anonymous. Opaque: this module does not interpret it.
 */
data class Member(
    val id: MemberId,
    val displayName: String,
    val joinedAt: Instant,
    val credentialId: String? = null,
)
