package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.document.GroupDocument
import com.cyrillrx.family.group.document.MemberDocument
import kotlinx.coroutines.flow.Flow

interface GroupStore {

    /** Throws when the store cannot answer, an absence it cannot confirm included. */
    suspend fun findGroupOf(credential: String): GroupLookup

    /**
     * Writes the group and its founder's membership together, or neither. Both times are the
     * server's: whatever `createdAt` and `joinedAt` the documents carry is not written.
     */
    suspend fun create(group: GroupDocument, founder: MemberDocument)

    /** The store refuses it while the group exists. */
    suspend fun leave(groupId: String, credential: String)

    fun observeMembers(groupId: String): Flow<List<MemberDocument>>
}

sealed interface GroupLookup {
    data object NoMembership : GroupLookup
    data class Found(val groupId: String, val group: GroupDocument) : GroupLookup
    data class NotFound(val groupId: String) : GroupLookup
}
