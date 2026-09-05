package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.domain.Group
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.IdGenerator
import com.cyrillrx.family.group.domain.Member
import com.cyrillrx.family.group.domain.MemberId
import com.cyrillrx.family.group.domain.UuidIdGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

/** Upholds the [GroupRepository] contract; latency and connectivity loss are not modelled. */
class RamGroupRepository(
    private val clock: Clock = Clock.System,
    private val idGenerator: IdGenerator = UuidIdGenerator,
) : GroupRepository {

    private val group = MutableStateFlow<Group?>(null)
    private val members = MutableStateFlow<List<Member>>(emptyList())

    override fun observeGroup(): Flow<Group?> = group.asStateFlow()

    override fun observeMembers(): Flow<List<Member>> = members.asStateFlow()

    override suspend fun createGroup(name: String, creator: Member): Group {
        val created = Group(
            id = idGenerator.newGroupId(),
            name = name,
            createdAt = clock.now(),
        )
        group.value = created
        members.value = listOf(creator)
        return created
    }

    override suspend fun addMember(member: Member) {
        members.update { current -> current.filterNot { it.id == member.id } + member }
    }

    override suspend fun removeMember(id: MemberId) {
        members.update { current -> current.filterNot { it.id == id } }
    }
}
