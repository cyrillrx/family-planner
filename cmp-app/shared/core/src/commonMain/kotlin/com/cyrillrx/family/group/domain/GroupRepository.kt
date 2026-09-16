package com.cyrillrx.family.group.domain

import com.cyrillrx.family.group.domain.model.Group
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.Member
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    /** Emits null until a group exists. */
    fun observeGroup(): Flow<Group?>

    fun observeMembers(groupId: GroupId): Flow<List<Member>>

    suspend fun setGroup(group: Group)

    suspend fun addMember(member: Member)

    suspend fun removeMember(groupId: GroupId, userId: UserId)
}
