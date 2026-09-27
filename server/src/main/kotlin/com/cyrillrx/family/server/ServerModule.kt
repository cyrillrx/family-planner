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

/** Falls back to the default when the variable is unset or not a number. */
internal fun serverPort(fromEnvironment: String?): Int =
    fromEnvironment?.toIntOrNull() ?: DEFAULT_PORT

internal const val DEFAULT_PORT = 8080
