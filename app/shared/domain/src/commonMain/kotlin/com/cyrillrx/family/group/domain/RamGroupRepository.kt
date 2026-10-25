package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.Member
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class RamGroupRepository : GroupRepository {

    private val group = MutableStateFlow<Group?>(null)
    private val members = MutableStateFlow<List<Member>>(emptyList())

    override suspend fun group(): Group? = group.value

    override fun observeMembers(groupId: GroupId): Flow<List<Member>> =
        members.map { current -> current.filter { it.groupId == groupId } }

    override suspend fun create(group: Group, founder: UserId): Result<Unit, CreateGroupWriteError> {
        setGroup(group)
        addMember(Member(userId = founder, groupId = group.id, joinedAt = group.createdAt))

        return Result.Success(Unit)
    }

    fun setGroup(group: Group) {
        this.group.value = group
    }

    fun addMember(member: Member) {
        members.update { current ->
            val alreadyAMember = current.any {
                it.groupId == member.groupId && it.userId == member.userId
            }

            if (alreadyAMember) current else current + member
        }
    }

    fun removeMember(groupId: GroupId, userId: UserId) {
        members.update { current ->
            current.filterNot { it.groupId == groupId && it.userId == userId }
        }
    }
}
