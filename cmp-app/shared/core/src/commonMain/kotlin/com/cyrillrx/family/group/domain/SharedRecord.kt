package com.cyrillrx.family.group.domain

import kotlin.time.Instant

/**
 * When a shared record last changed, and who changed it.
 *
 * [updatedAt] is server time, not device time. Two devices with drifting clocks would otherwise
 * decide conflicts differently, and PRD-001 requires the same result on every device.
 */
data class Revision(
    val updatedAt: Instant,
    val updatedBy: MemberId,
)

/**
 * What every shared record carries: the group it belongs to, and its revision (PRD-001).
 *
 * [deleted] makes a deletion a version of the record rather than its absence. A record that simply
 * vanished could not be told apart from one a device has not received yet, so a returning device
 * would resurrect it.
 */
interface SharedRecord {
    val groupId: GroupId
    val revision: Revision
    val deleted: Boolean
}
