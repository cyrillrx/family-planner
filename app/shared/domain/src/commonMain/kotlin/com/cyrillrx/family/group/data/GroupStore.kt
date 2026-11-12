package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.document.GroupDocument
import com.cyrillrx.family.group.document.MemberDocument
import com.cyrillrx.family.group.domain.StoreUnavailable
import kotlinx.coroutines.flow.Flow

interface GroupStore {

    /** Fails when the store cannot answer, an absence it cannot confirm included. */
    suspend fun findGroupOf(credential: String): Result<GroupLookup, StoreUnavailable>

    /**
     * Writes the group and its founder's membership together, or neither. Both times are the
     * server's: whatever `createdAt` and `joinedAt` the documents carry is not written.
     */
    suspend fun create(group: GroupDocument, founder: MemberDocument): Result<Unit, StoreUnavailable>

    /** The store refuses it while the group exists. */
    suspend fun leave(groupId: String, credential: String): Result<Unit, StoreUnavailable>

    fun observeMembers(groupId: String): Flow<List<MemberDocument>>
}

sealed interface GroupLookup {
    data object NoMembership : GroupLookup
    data class Found(val groupId: String, val group: GroupDocument) : GroupLookup
    data class NotFound(val groupId: String) : GroupLookup
}
