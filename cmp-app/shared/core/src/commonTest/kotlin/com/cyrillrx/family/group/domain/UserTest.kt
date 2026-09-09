package com.cyrillrx.family.group.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UserTest {

    @Test
    fun `cannot be nameless`() {
        assertFailsWith<IllegalArgumentException> { user().copy(displayName = "") }
    }

    @Test
    fun `cannot be named with whitespace alone`() {
        assertFailsWith<IllegalArgumentException> { user().copy(displayName = "   ") }
    }

    @Test
    fun `is unauthenticated until Phase 2`() {
        assertNull(user().authenticatedId)
    }

    @Test
    fun `holds the credential beside its own identifier`() {
        val authenticated = user().copy(authenticatedId = "firebase-uid")

        assertEquals(UserId("user-1"), authenticated.id)
        assertEquals("firebase-uid", authenticated.authenticatedId)
    }

    private fun user() = User(id = UserId("user-1"), displayName = "Cyril")
}
