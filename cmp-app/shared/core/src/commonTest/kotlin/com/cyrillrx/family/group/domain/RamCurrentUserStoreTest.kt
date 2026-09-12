package com.cyrillrx.family.group.domain

import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RamCurrentUserStoreTest {

    @Test
    fun `knows nobody until a user is remembered`() = runTest {
        assertNull(RamCurrentUserStore().observeCurrentUserId().first())
    }

    @Test
    fun `emits the user it was told to remember`() = runTest {
        val store = RamCurrentUserStore()

        store.setCurrentUserId(UserId("user-1"))

        assertEquals(UserId("user-1"), store.observeCurrentUserId().first())
    }

    @Test
    fun `starts from the user it was built with`() = runTest {
        val store = RamCurrentUserStore(UserId("user-1"))

        assertEquals(UserId("user-1"), store.observeCurrentUserId().first())
    }

    @Test
    fun `replaces the user it remembered`() = runTest {
        val store = RamCurrentUserStore(UserId("user-1"))

        store.setCurrentUserId(UserId("user-2"))

        assertEquals(UserId("user-2"), store.observeCurrentUserId().first())
    }
}
