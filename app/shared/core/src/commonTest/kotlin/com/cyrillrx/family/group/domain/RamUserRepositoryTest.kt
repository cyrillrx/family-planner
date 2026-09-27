package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RamUserRepositoryTest {

    @Test
    fun `has no registered user before anyone registers`() = runTest {
        assertNull(RamUserRepository().registeredUserId())
    }

    @Test
    fun `answers with the user it was given`() = runTest {
        val repository = RamUserRepository()

        val registered = repository.register(alice())

        assertEquals(Result.Success(alice()), registered)
    }

    @Test
    fun `holds the id of the registered user`() = runTest {
        val repository = RamUserRepository()

        repository.register(alice())

        assertEquals(UserId("alice"), repository.registeredUserId())
    }

    @Test
    fun `registering again replaces the previous registration`() = runTest {
        val repository = RamUserRepository()
        repository.register(alice())

        repository.register(User(id = UserId("bob"), displayName = "Bob"))

        assertEquals(UserId("bob"), repository.registeredUserId())
    }

    private fun alice() = User(id = UserId("alice"), displayName = "Alice")
}
