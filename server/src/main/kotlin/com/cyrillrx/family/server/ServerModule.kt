package com.cyrillrx.family.server

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.module() {
    routing {
        get("/health") { call.respondText("OK") }
    }
}

/** Falls back to the default when the variable is unset, not a number, or out of range. */
internal fun serverPort(fromEnvironment: String?): Int =
    fromEnvironment?.toIntOrNull()?.takeIf { it in 1..65535 } ?: DEFAULT_PORT

internal const val DEFAULT_PORT = 8080
