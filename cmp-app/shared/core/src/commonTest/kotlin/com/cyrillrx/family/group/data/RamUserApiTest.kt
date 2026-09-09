package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.domain.User
import com.cyrillrx.family.group.domain.UserId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RamUserApiTest {

    @Test
    fun `answers a registration with the registered user`() = runTest {
        val registered = RamUserApi().register(request("user-1", "Cyril"))

        assertEquals("user-1", registered.id)
        assertEquals("Cyril", registered.displayName)
    }

    @Test
    fun `leaves the credential to Phase 2`() = runTest {
        assertNull(RamUserApi().register(request("user-1", "Cyril")).authenticatedId)
    }

    @Test
    fun `makes a registered user observable`() = runTest {
        val api = RamUserApi()

        api.register(request("user-1", "Cyril"))

        assertEquals(
            listOf(ApiUser(id = "user-1", displayName = "Cyril")),
            api.observeUsers(setOf("user-1")).first(),
        )
    }

    @Test
    fun `registering the same identifier twice updates the name instead of duplicating`() = runTest {
        val api = RamUserApi()

        api.register(request("user-1", "Cyril"))
        api.register(request("user-1", "Cyril L"))

        assertEquals(
            listOf(ApiUser(id = "user-1", displayName = "Cyril L")),
            api.observeUsers(setOf("user-1")).first(),
        )
    }

    @Test
    fun `answers with the users asked for and no others`() = runTest {
        val api = RamUserApi(listOf(apiUser("user-1"), apiUser("user-2"), apiUser("user-3")))

        assertEquals(
            listOf(apiUser("user-1"), apiUser("user-3")),
            api.observeUsers(setOf("user-1", "user-3")).first(),
        )
    }

    @Test
    fun `answers nothing when nothing is asked for`() = runTest {
        val api = RamUserApi(listOf(apiUser("user-1")))

        assertEquals(emptyList(), api.observeUsers(emptySet()).first())
    }

    @Test
    fun `skips an identifier it does not know`() = runTest {
        val api = RamUserApi(listOf(apiUser("user-1")))

        assertEquals(
            listOf(apiUser("user-1")),
            api.observeUsers(setOf("user-1", "nobody")).first(),
        )
    }

    @Test
    fun `maps a user to its registration request`() {
        val request = User(id = UserId("user-1"), displayName = "Cyril").toRegisterRequest()

        assertEquals(ApiRegisterUserRequest(id = "user-1", displayName = "Cyril"), request)
    }

    @Test
    fun `maps an answered user back to the domain`() {
        val user = ApiUser(id = "user-1", displayName = "Cyril", authenticatedId = "uid").toDomain()

        assertEquals(User(UserId("user-1"), "Cyril", authenticatedId = "uid"), user)
    }

    private fun request(id: String, displayName: String) =
        ApiRegisterUserRequest(id = id, displayName = displayName)

    private fun apiUser(id: String) = ApiUser(id = id, displayName = id)
}
