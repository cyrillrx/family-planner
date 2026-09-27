package com.cyrillrx.family.group.api

data class ApiRegisterUserRequest(
    val id: String,
    val displayName: String,
)

data class ApiUser(
    val id: String? = null,
    val displayName: String? = null,
    val authenticatedId: String? = null,
)
