package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.Member
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    /** Succeeds with null when this device belongs to no group. */
    suspend fun group(): Result<Group?, GroupReadError>

    fun observeMembers(groupId: GroupId): Flow<List<Member>>

    suspend fun create(group: Group, founder: UserId): Result<Unit, CreateGroupWriteError>

    /**
     * Leaves the group that [group] answers [GroupNotFound] for. Succeeds when there is none to
     * leave, and never leaves a group that exists.
     */
    suspend fun leaveMissingGroup(): Result<Unit, StoreUnavailable>
}

sealed interface GroupReadError : Error

/** This device's membership names a group that does not exist: the member can only leave it. */
data object GroupNotFound : GroupReadError, CreateGroupWriteError

sealed interface CreateGroupWriteError : Error {
    data object AlreadyInAGroup : CreateGroupWriteError
    data object Unknown : CreateGroupWriteError
}
