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
import kotlin.test.assertFailsWith
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

        assertEquals(emptyList(), repository.observeMembers().first())
    }

    @Test
    fun `refuses to create a second group`() = runTest {
        val repository = repository()
        repository.createGroup("Home", member("alice"))

        assertFailsWith<IllegalStateException> {
            repository.createGroup("Other", member("bob"))
        }
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
