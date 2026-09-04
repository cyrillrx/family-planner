package com.cyrillrx.family.group.domain

import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes the group and its members.
 *
 * Reads are [Flow]s rather than one-shot calls: PRD-001 requires a change made by one member to
 * reach the others without anyone refreshing, so observation is the normal way to read and a
 * snapshot is the exception.
 *
 * No type from any storage provider appears in this interface, which ADR-003 requires: it is what
 * keeps the conflict and offline behaviour verifiable in `shared/core` without a network, and what
 * stops a change of provider from reaching the call sites.
 */
interface GroupRepository {

    /** Emits the group, then again on every change. Emits null until one exists. */
    fun observeGroup(): Flow<Group?>

    /**
     * Emits the group's members, then again on every change.
     *
     * A group of one emits a list of one. Nothing downstream may treat that as an empty state:
     * PRD-001 requires a group of one to behave exactly like a group of five.
     */
    fun observeMembers(): Flow<List<Member>>

    /** Creates the group with [creator] as its first member. */
    suspend fun createGroup(name: String, creator: Member): Group

    /** Adds [member] to the group. Redeeming an invitation is what authorises the call. */
    suspend fun addMember(member: Member)

    /** Removes the member [id] from the group. */
    suspend fun removeMember(id: MemberId)
}
