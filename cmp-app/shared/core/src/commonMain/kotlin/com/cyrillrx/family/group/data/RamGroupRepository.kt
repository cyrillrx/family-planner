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

/**
 * Holds the group in memory. Writes are kept for as long as the instance lives.
 *
 * This belongs to the main source set, not to a test one: Compose previews depend on it, and
 * ViewModel tests reuse it rather than declaring a double of their own.
 *
 * It is not a stand-in for the storage provider. It upholds the behaviour `GroupRepository`
 * promises so that behaviour can be asserted without a network — which PRD-001 requires — and
 * nothing more. Latency, connectivity loss and cross-device propagation are not modelled here.
 *
 * [clock] is injected so a test can decide what "now" is instead of waiting for it.
 */
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
        // Replaces rather than appends when the id is already known, so a repeated call cannot
        // put the same member in the group twice.
        members.update { current ->
            current.filterNot { it.id == member.id } + member
        }
    }

    override suspend fun removeMember(id: MemberId) {
        members.update { current -> current.filterNot { it.id == id } }
    }
}
