package com.cyrillrx.family.group.domain

import kotlin.time.Instant

data class Member(
    val id: MemberId,
    val displayName: String,
    val joinedAt: Instant,
    /** Opaque to this module. Null while the member is anonymous. */
    val credentialId: String? = null,
)
