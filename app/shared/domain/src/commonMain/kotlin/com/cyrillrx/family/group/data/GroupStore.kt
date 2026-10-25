package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.document.GroupDocument
import com.cyrillrx.family.group.document.MemberDocument
import kotlinx.coroutines.flow.Flow

/**
 * The groups this device can reach, and their memberships.
 *
 * Every call may throw when the store refuses or cannot be reached, under the read and write
 * contract [UserStore] states.
 */
interface GroupStore {

    /** The group the credential is a member of, or null if it is a member of none. */
    suspend fun findGroupOf(credential: String): GroupDocument?

    /**
     * Writes the group and its founder's membership together, or neither. Both times are the
     * server's: whatever `createdAt` and `joinedAt` the documents carry is not written.
     */
    suspend fun create(group: GroupDocument, founder: MemberDocument)

    /** Emits again whenever a membership of the group changes. */
    fun observeMembers(groupId: String): Flow<List<MemberDocument>>
}
