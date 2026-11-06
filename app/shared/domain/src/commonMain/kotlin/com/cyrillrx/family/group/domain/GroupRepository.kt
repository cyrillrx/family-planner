package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.Member
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    suspend fun group(): Group?

    fun observeMembers(groupId: GroupId): Flow<List<Member>>

    suspend fun create(group: Group, founder: UserId): Result<Unit, CreateGroupWriteError>
}

sealed interface CreateGroupWriteError : Error {
    data object AlreadyInAGroup : CreateGroupWriteError
    data object Unknown : CreateGroupWriteError
}
