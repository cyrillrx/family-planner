package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.Member
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    /** Null until this device's user belongs to a group. */
    suspend fun group(): Group?

    fun observeMembers(groupId: GroupId): Flow<List<Member>>

    /** Writes the group and its founder's membership together, or neither. */
    suspend fun create(group: Group, founder: UserId): Result<Unit, CreateGroupWriteError>
}

sealed interface CreateGroupWriteError : Error {
    data object Unknown : CreateGroupWriteError
}
