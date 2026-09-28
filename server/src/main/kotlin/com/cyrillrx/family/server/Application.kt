package com.cyrillrx.family.server

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

/**
 * The entry point, and nothing else: starting an engine is what no test does, so everything
 * worth measuring lives in [module] instead. Excluded from coverage on both sides.
 */
fun main() {
    embeddedServer(
        factory = Netty,
        port = serverPort(System.getenv("PORT")),
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}
