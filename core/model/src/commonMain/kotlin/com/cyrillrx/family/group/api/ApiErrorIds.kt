package com.cyrillrx.family.group.api

/**
 * What `ApiError.id` carries when the service refuses. Fixed by ADR-005, so both sides rename
 * them together or neither does. An id the client does not recognise maps to its own Unknown,
 * which is what lets the service add one without a client release.
 */
const val INVITATION_UNKNOWN = "invitation_unknown"
const val INVITATION_REVOKED = "invitation_revoked"
const val INVITATION_ALREADY_REDEEMED = "invitation_already_redeemed"
const val INVITATION_EXPIRED = "invitation_expired"
const val USER_ALREADY_IN_A_GROUP = "user_already_in_a_group"
