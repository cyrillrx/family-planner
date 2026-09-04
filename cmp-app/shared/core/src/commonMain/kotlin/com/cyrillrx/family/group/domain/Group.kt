package com.cyrillrx.family.group.domain

import kotlin.time.Instant

/**
 * The unit that owns every piece of shared data.
 *
 * A group may hold one member or several, and behaves identically either way: nothing here, and
 * nothing that reads it, may assume a number of members (PRD-001).
 *
 * [name] carries a generated default and is displayed nowhere in V1. It exists so that telling one
 * group from another is possible the day a member belongs to more than one.
 */
data class Group(
    val id: GroupId,
    val name: String,
    val createdAt: Instant,
)

/**
 * A person in a group, on one device.
 *
 * [credentialId] is opaque on purpose. It holds whatever the authentication provider uses to
 * recognise this member — this module neither knows nor cares what that is — and is null for as
 * long as the member is anonymous, which is all of PRD-001 Phase 1. [id] is the identity the rest
 * of the product refers to, and it does not change when a credential is attached later.
 *
 * [displayName] is the only thing about a member that the others see.
 */
data class Member(
    val id: MemberId,
    val displayName: String,
    val joinedAt: Instant,
    val credentialId: String? = null,
)
