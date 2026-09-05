package com.cyrillrx.family.group.domain

import kotlin.time.Instant

data class Revision(
    /** Server time: device clocks drift, and drifting clocks resolve conflicts differently. */
    val updatedAt: Instant,
    val updatedBy: MemberId,
)

interface SharedRecord {
    val groupId: GroupId
    val revision: Revision

    /** A version of the record, not its absence — absence means not-yet-received. */
    val deleted: Boolean
}
