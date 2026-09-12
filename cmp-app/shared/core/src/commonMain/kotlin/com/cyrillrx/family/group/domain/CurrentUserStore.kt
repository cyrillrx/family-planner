package com.cyrillrx.family.group.domain

import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.Flow

/**
 * Who this device is. Only the identifier: the user itself lives in [UserRepository] and can change,
 * and a second copy here would be a second truth.
 *
 * A remembered identifier is what says onboarding is behind us — not the presence of a group, which
 * stays null until the group syncs and would send someone who just joined back to creating one.
 */
interface CurrentUserStore {

    /** Emits null until this device has a registered user. */
    fun observeCurrentUserId(): Flow<UserId?>

    suspend fun setCurrentUserId(id: UserId)
}
