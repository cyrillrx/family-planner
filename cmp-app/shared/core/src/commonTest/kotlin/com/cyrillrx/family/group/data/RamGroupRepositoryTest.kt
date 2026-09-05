package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.domain.Group
import com.cyrillrx.family.group.domain.GroupId
import com.cyrillrx.family.group.domain.Member
import com.cyrillrx.family.group.domain.MemberId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class RamGroupRepositoryTest {

    @Test
    fun `has no group before one is set`() = runTest {
        assertNull(RamGroupRepository().observeGroup().first())
    }

    @Test
    fun `emits the group once set`() = runTest {
        val repository = RamGroupRepository()

        repository.setGroup(group())

        assertEquals(group(), repository.observeGroup().first())
    }

    @Test
    fun `replaces the group when set again`() = runTest {
        val repository = RamGroupRepository()
        repository.setGroup(group())

        repository.setGroup(group().copy(name = "Other"))

        assertEquals("Other", repository.observeGroup().first()?.name)
    }

    @Test
    fun `has no members before any is saved`() = runTest {
        assertEquals(emptyList(), RamGroupRepository().observeMembers().first())
    }

    @Test
    fun `a group of one emits a list of one`() = runTest {
        val repository = RamGroupRepository()
        repository.setGroup(group())

        repository.saveMember(member("alice"))

        assertEquals(listOf(member("alice")), repository.observeMembers().first())
    }

    @Test
    fun `emits the new list after a member joins`() = runTest {
        val repository = RamGroupRepository()
        repository.saveMember(member("alice"))

        repository.saveMember(member("bob"))

        assertEquals(listOf(member("alice"), member("bob")), repository.observeMembers().first())
    }

    @Test
    fun `saving the same member twice does not duplicate them`() = runTest {
        val repository = RamGroupRepository()

        repository.saveMember(member("bob"))
        repository.saveMember(member("bob"))

        assertEquals(1, repository.observeMembers().first().size)
    }

    @Test
    fun `saving an existing member replaces their record`() = runTest {
        val repository = RamGroupRepository()
        repository.saveMember(member("bob"))

        repository.saveMember(member("bob").copy(displayName = "Bobby"))

        assertEquals("Bobby", repository.observeMembers().first().single().displayName)
    }

    @Test
    fun `emits the new list after a member is removed`() = runTest {
        val repository = RamGroupRepository()
        repository.saveMember(member("alice"))
        repository.saveMember(member("bob"))

        repository.removeMember(MemberId("bob"))

        assertEquals(listOf(member("alice")), repository.observeMembers().first())
    }

    @Test
    fun `removing an unknown member changes nothing`() = runTest {
        val repository = RamGroupRepository()
        repository.saveMember(member("alice"))

        repository.removeMember(MemberId("nobody"))

        assertEquals(listOf(member("alice")), repository.observeMembers().first())
    }

    @Test
    fun `removing the last member leaves the group empty`() = runTest {
        val repository = RamGroupRepository()
        repository.saveMember(member("alice"))

        repository.removeMember(MemberId("alice"))

        assertEquals(emptyList(), repository.observeMembers().first())
    }

    private fun group() = Group(
        id = GroupId("group-1"),
        name = "Home",
        createdAt = FIXED_NOW,
    )

    private fun member(id: String) = Member(
        id = MemberId(id),
        displayName = id,
        joinedAt = FIXED_NOW,
    )

    private companion object {
        val FIXED_NOW: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
