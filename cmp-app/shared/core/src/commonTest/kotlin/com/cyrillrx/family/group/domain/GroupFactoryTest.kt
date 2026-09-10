package com.cyrillrx.family.group.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Clock
import kotlin.time.Instant

class GroupFactoryTest {

    @Test
    fun `takes its identifier from the generator`() {
        assertEquals(GroupId("group-1"), factory().newGroup().id)
    }

    @Test
    fun `takes its creation instant from the clock`() {
        assertEquals(NOW, factory().newGroup().createdAt)
    }

    @Test
    fun `names the group without asking anyone`() {
        assertEquals("Family", factory().newGroup().name)
    }

    @Test
    fun `gives two groups two identifiers`() {
        val factory = factory()

        assertNotEquals(factory.newGroup().id, factory.newGroup().id)
    }

    private fun factory() = GroupFactory(ids = SequentialIdGenerator(), clock = FixedClock)

    private class SequentialIdGenerator : IdGenerator {
        private var groups = 0

        override fun newGroupId() = GroupId("group-${++groups}")

        override fun newUserId() = UserId("user-1")

        override fun newInvitationId() = InvitationId("invitation-1")
    }

    private object FixedClock : Clock {
        override fun now(): Instant = NOW
    }

    private companion object {
        val NOW: Instant = Instant.fromEpochMilliseconds(1_500)
    }
}
