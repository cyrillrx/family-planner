package com.cyrillrx.family.group.domain

import com.cyrillrx.family.group.domain.model.Group
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.Member
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class RamGroupRepository : GroupRepository {

    private val group = MutableStateFlow<Group?>(null)
    private val members = MutableStateFlow<List<Member>>(emptyList())

    override fun observeGroup(): Flow<Group?> = group.asStateFlow()

    override fun observeMembers(groupId: GroupId): Flow<List<Member>> =
        members.map { current -> current.filter { it.groupId == groupId } }

    override suspend fun setGroup(group: Group) {
        this.group.value = group
    }

    override suspend fun addMember(member: Member) {
        members.update { current ->
            val alreadyAMember = current.any {
                it.groupId == member.groupId && it.userId == member.userId
            }

            if (alreadyAMember) current else current + member
        }
    }

    override suspend fun removeMember(groupId: GroupId, userId: UserId) {
        members.update { current ->
            current.filterNot { it.groupId == groupId && it.userId == userId }
        }
    }
}
