package com.cyrillrx.family.group.domain

import kotlin.time.Instant

/**
 * When a shared record last changed, and who changed it.
 *
 * @param updatedAt server time. Device clocks drift, and drifting clocks resolve conflicts
 *   differently on each device.
 */
data class Revision(
    val updatedAt: Instant,
    val updatedBy: MemberId,
)

/**
 * What every shared record carries.
 *
 * @property deleted a deletion is a version of the record, not its absence — absence cannot be
 *   told apart from not-yet-received.
 */
interface SharedRecord {
    val groupId: GroupId
    val revision: Revision
    val deleted: Boolean
}
