package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.domain.GroupId
import com.cyrillrx.family.group.domain.IdGenerator
import com.cyrillrx.family.group.domain.InvitationId
import com.cyrillrx.family.group.domain.Member
import com.cyrillrx.family.group.domain.MemberId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

class RamGroupRepositoryTest {

    @Test
    fun `has no group before one is created`() = runTest {
        assertNull(repository().observeGroup().first())
    }

    @Test
    fun `emits the group once created`() = runTest {
        val repository = repository()

        repository.createGroup("Home", member("alice"))

        val group = repository.observeGroup().first()
        assertEquals("Home", group?.name)
        assertEquals(GroupId("group-1"), group?.id)
        assertEquals(FIXED_NOW, group?.createdAt)
    }

    @Test
    fun `creating a group puts the creator in it`() = runTest {
        val repository = repository()

        repository.createGroup("Home", member("alice"))

        assertEquals(listOf(member("alice")), repository.observeMembers().first())
    }

    @Test
    fun `a group of one emits a list of one`() = runTest {
        val repository = repository()
        repository.createGroup("Home", member("alice"))

        // PRD-001 requires a group of one to behave exactly like a group of five. An empty list
        // here would push every caller into treating a solo group as an empty state.
        assertEquals(1, repository.observeMembers().first().size)
    }

    @Test
    fun `emits the new list after a member joins`() = runTest {
        val repository = repository()
        repository.createGroup("Home", member("alice"))

        repository.addMember(member("bob"))

        assertEquals(
            listOf(member("alice"), member("bob")),
            repository.observeMembers().first(),
        )
    }

    @Test
    fun `adding the same member twice does not duplicate them`() = runTest {
        val repository = repository()
        repository.createGroup("Home", member("alice"))

        repository.addMember(member("bob"))
        repository.addMember(member("bob"))

        assertEquals(2, repository.observeMembers().first().size)
    }

    @Test
    fun `adding an existing member replaces their record`() = runTest {
        val repository = repository()
        repository.createGroup("Home", member("alice"))

        repository.addMember(member("bob").copy(displayName = "Bobby"))

        val members = repository.observeMembers().first()
        assertEquals("Bobby", members.single { it.id == MemberId("bob") }.displayName)
    }

    @Test
    fun `emits the new list after a member is removed`() = runTest {
        val repository = repository()
        repository.createGroup("Home", member("alice"))
        repository.addMember(member("bob"))

        repository.removeMember(MemberId("bob"))

        assertEquals(listOf(member("alice")), repository.observeMembers().first())
    }

    @Test
    fun `removing an unknown member changes nothing`() = runTest {
        val repository = repository()
        repository.createGroup("Home", member("alice"))

        repository.removeMember(MemberId("nobody"))

        assertEquals(listOf(member("alice")), repository.observeMembers().first())
    }

    @Test
    fun `removing the last member leaves the group empty`() = runTest {
        val repository = repository()
        repository.createGroup("Home", member("alice"))

        repository.removeMember(MemberId("alice"))

        // Deleting the group when it reaches empty is PRD-001 Phase 2 and deliberately not done
        // here. The repository has to make the empty state reachable for that to be built.
        assertEquals(emptyList(), repository.observeMembers().first())
    }

    private fun repository() = RamGroupRepository(
        clock = FixedClock,
        idGenerator = CountingIdGenerator(),
    )

    private fun member(id: String) = Member(
        id = MemberId(id),
        displayName = id,
        joinedAt = FIXED_NOW,
    )

    /** Counts rather than randomises, so an assertion can name the identifier it expects. */
    private class CountingIdGenerator : IdGenerator {
        private var groups = 0
        private var members = 0
        private var invitations = 0

        override fun newGroupId() = GroupId("group-${++groups}")

        override fun newMemberId() = MemberId("member-${++members}")

        override fun newInvitationId() = InvitationId("invitation-${++invitations}")
    }

    private object FixedClock : Clock {
        override fun now(): Instant = FIXED_NOW
    }

    private companion object {
        val FIXED_NOW: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
