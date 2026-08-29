package com.cyrillrx.family

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform