package com.cyrillrx.family.server

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerModuleTest {

    @Test
    fun `answers the health check`() = testApplication {
        application { module() }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }

    @Test
    fun `serves no route beyond the health check yet`() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.NotFound, client.get("/").status)
    }

    @Test
    fun `reads the port from the environment`() {
        assertEquals(9000, serverPort("9000"))
    }

    @Test
    fun `falls back to the default port when the environment says nothing usable`() {
        val unusable = listOf(null, "", "   ", "http", "-1x")

        unusable.forEach { value ->
            assertEquals(DEFAULT_PORT, serverPort(value), "'$value' names no port")
        }
    }
}
