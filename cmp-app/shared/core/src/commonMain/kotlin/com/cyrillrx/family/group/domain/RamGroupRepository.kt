package com.cyrillrx.family.group.domain

import com.cyrillrx.family.group.domain.model.Group
import com.cyrillrx.family.group.domain.model.Member
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RamGroupRepository : GroupRepository {

    private val group = MutableStateFlow<Group?>(null)
    private val members = MutableStateFlow<List<Member>>(emptyList())

    override fun observeGroup(): Flow<Group?> = group.asStateFlow()

    override fun observeMembers(): Flow<List<Member>> = members.asStateFlow()

    override suspend fun setGroup(group: Group) {
        this.group.value = group
    }

    override suspend fun addMember(member: Member) {
        members.update { current ->
            if (current.any { it.id == member.id }) current else current + member
        }
    }

    override suspend fun removeMember(userId: UserId) {
        members.update { current -> current.filterNot { it.id == userId } }
    }
}
