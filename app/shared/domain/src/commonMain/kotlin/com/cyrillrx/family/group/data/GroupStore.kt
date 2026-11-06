package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.document.GroupDocument
import com.cyrillrx.family.group.document.MemberDocument
import kotlinx.coroutines.flow.Flow

interface GroupStore {

    suspend fun findGroupOf(credential: String): GroupDocument?

    /**
     * Writes the group and its founder's membership together, or neither. Both times are the
     * server's: whatever `createdAt` and `joinedAt` the documents carry is not written.
     */
    suspend fun create(group: GroupDocument, founder: MemberDocument)

    fun observeMembers(groupId: String): Flow<List<MemberDocument>>
}
