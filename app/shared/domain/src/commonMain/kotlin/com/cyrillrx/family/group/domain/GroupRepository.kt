package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.Member
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    suspend fun group(): Result<Membership, GroupError.Read>

    fun observeMembers(groupId: GroupId): Flow<List<Member>>

    suspend fun create(group: Group, founder: UserId): Result<Unit, GroupError.Write>

    suspend fun leaveMissingGroup(): Result<Unit, NoAnswer>
}

sealed interface Membership {
    data class InGroup(val group: Group) : Membership
    data object NoGroup : Membership
}
