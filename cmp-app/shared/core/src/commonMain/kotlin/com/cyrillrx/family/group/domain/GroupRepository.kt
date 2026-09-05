package com.cyrillrx.family.group.domain

import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    /** Emits null until a group exists. */
    fun observeGroup(): Flow<Group?>

    fun observeMembers(): Flow<List<Member>>

    suspend fun setGroup(group: Group)

    suspend fun saveMember(member: Member)

    suspend fun removeMember(id: MemberId)
}
