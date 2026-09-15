package com.cyrillrx.family.group.domain.model

data class User(
    val id: UserId,
    val displayName: String,
    val authenticatedId: String? = null,
) {
    init {
        require(displayName.isNotBlank()) {
            "A user without a display name is invisible to the other members"
        }
    }
}
