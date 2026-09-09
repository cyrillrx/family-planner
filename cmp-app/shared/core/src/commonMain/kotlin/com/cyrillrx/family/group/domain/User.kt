package com.cyrillrx.family.group.domain

/**
 * A person, independently of any group. [authenticatedId] is the credential that will authenticate
 * this identity in Phase 2, held beside it rather than instead of it (ADR-003), and null until then.
 */
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
