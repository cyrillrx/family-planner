package com.cyrillrx.family.group.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class ConflictResolutionTest {

    @Test
    fun `keeps the later of two edits`() {
        val older = record(at = 100, by = "alice", label = "milk")
        val newer = record(at = 200, by = "bob", label = "oat milk")

        assertEquals("oat milk", resolveConflict(older, newer).label)
    }

    @Test
    fun `a deletion landing after an edit removes the record`() {
        val edit = record(at = 100, by = "alice")
        val deletion = record(at = 200, by = "bob", deleted = true)

        assertTrue(resolveConflict(edit, deletion).deleted)
    }

    @Test
    fun `an edit landing after a deletion keeps the record`() {
        val deletion = record(at = 100, by = "alice", deleted = true)
        val edit = record(at = 200, by = "bob")

        assertFalse(resolveConflict(deletion, edit).deleted)
    }

    @Test
    fun `gives the same winner whatever order the versions arrive in`() {
        val older = record(at = 100, by = "alice", label = "milk")
        val newer = record(at = 200, by = "bob", label = "oat milk")

        assertEquals(resolveConflict(older, newer), resolveConflict(newer, older))
    }

    @Test
    fun `gives the same winner on equal timestamps whatever the order`() {
        val one = record(at = 100, by = "alice", label = "milk")
        val other = record(at = 100, by = "bob", label = "oat milk")

        // The rule has to hold here too, or two devices receiving these in opposite orders
        // would settle on different values and never converge.
        assertEquals(resolveConflict(one, other), resolveConflict(other, one))
    }

    @Test
    fun `resolves an equal timestamp on the author`() {
        val fromAlice = record(at = 100, by = "alice", label = "milk")
        val fromBob = record(at = 100, by = "bob", label = "oat milk")

        assertEquals("oat milk", resolveConflict(fromAlice, fromBob).label)
    }

    @Test
    fun `returns an equivalent version when timestamp and author both match`() {
        val one = record(at = 100, by = "alice", label = "milk")
        val other = record(at = 100, by = "alice", label = "milk")

        assertEquals(one, resolveConflict(one, other))
    }

    private fun record(
        at: Long,
        by: String,
        label: String = "milk",
        deleted: Boolean = false,
    ) = TestRecord(
        groupId = GroupId("group-1"),
        revision = Revision(Instant.fromEpochMilliseconds(at), MemberId(by)),
        deleted = deleted,
        label = label,
    )

    private data class TestRecord(
        override val groupId: GroupId,
        override val revision: Revision,
        override val deleted: Boolean,
        val label: String,
    ) : SharedRecord
}
