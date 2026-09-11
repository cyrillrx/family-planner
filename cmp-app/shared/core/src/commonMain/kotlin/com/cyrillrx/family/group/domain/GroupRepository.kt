package com.cyrillrx.family.group.domain

import com.cyrillrx.family.group.domain.model.Group
import com.cyrillrx.family.group.domain.model.Member
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    /** Emits null until a group exists. */
    fun observeGroup(): Flow<Group?>

    fun observeMembers(): Flow<List<Member>>

    suspend fun setGroup(group: Group)

    suspend fun addMember(member: Member)

    suspend fun removeMember(userId: UserId)
}
