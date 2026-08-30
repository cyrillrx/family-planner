package com.cyrillrx.family

/** The platform the client currently runs on. */
interface Platform {
    val name: String
}

/** Returns the platform of the current target. */
expect fun getPlatform(): Platform
