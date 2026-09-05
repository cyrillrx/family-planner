package com.cyrillrx.family.group.domain

import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    /** Emits null until a group exists. */
    fun observeGroup(): Flow<Group?>

    fun observeMembers(): Flow<List<Member>>

    suspend fun createGroup(name: String, creator: Member): Group

    /** Adds [member], or replaces them if the group already holds that identity. */
    suspend fun addMember(member: Member)

    suspend fun removeMember(id: MemberId)
}
