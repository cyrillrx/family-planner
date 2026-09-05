package com.cyrillrx.family.group.domain

import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes the group and its members.
 *
 * Reads are observed rather than fetched: a change made by one member reaches the others without
 * anyone refreshing. No storage type appears here, so the store can be replaced without reaching
 * the call sites.
 */
interface GroupRepository {

    /** Emits null until a group exists. */
    fun observeGroup(): Flow<Group?>

    /** A group of one emits a list of one; it is never an empty state. */
    fun observeMembers(): Flow<List<Member>>

    /** Creates the group with [creator] as its first member. */
    suspend fun createGroup(name: String, creator: Member): Group

    /** Adds [member], or replaces them if the group already holds that identity. */
    suspend fun addMember(member: Member)

    suspend fun removeMember(id: MemberId)
}
