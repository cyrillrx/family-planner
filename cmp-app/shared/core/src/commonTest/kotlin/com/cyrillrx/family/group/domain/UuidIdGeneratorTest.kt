package com.cyrillrx.family.group.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class UuidIdGeneratorTest {

    @Test
    fun `never returns the same identifier twice`() {
        val groups = List(COUNT) { UuidIdGenerator.newGroupId() }
        val members = List(COUNT) { UuidIdGenerator.newMemberId() }
        val invitations = List(COUNT) { UuidIdGenerator.newInvitationId() }

        assertEquals(COUNT, groups.toSet().size)
        assertEquals(COUNT, members.toSet().size)
        assertEquals(COUNT, invitations.toSet().size)
    }

    @Test
    fun `does not draw from a shared sequence`() {
        val group = UuidIdGenerator.newGroupId()
        val member = UuidIdGenerator.newMemberId()
        val invitation = UuidIdGenerator.newInvitationId()

        assertEquals(3, setOf(group.value, member.value, invitation.value).size)
    }

    @Test
    fun `produces identifiers the length of a uuid`() {
        assertEquals(UUID_LENGTH, UuidIdGenerator.newGroupId().value.length)
        assertEquals(UUID_LENGTH, UuidIdGenerator.newMemberId().value.length)
        assertEquals(UUID_LENGTH, UuidIdGenerator.newInvitationId().value.length)
    }

    private companion object {
        const val COUNT = 100

        const val UUID_LENGTH = 36
    }
}
