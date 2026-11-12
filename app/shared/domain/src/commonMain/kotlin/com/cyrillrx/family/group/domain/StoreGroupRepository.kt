package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.core.domain.getOrElse
import com.cyrillrx.family.group.data.GroupLookup
import com.cyrillrx.family.group.data.GroupStore
import com.cyrillrx.family.group.data.UserStore
import com.cyrillrx.family.group.document.GroupDocument
import com.cyrillrx.family.group.document.MemberDocument
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.Member
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant

class StoreGroupRepository(
    private val store: GroupStore,
    private val users: UserStore,
) : GroupRepository {

    override suspend fun group(): Result<Group?, GroupReadError> {
        val lookup = reachStore { users.credential()?.let { store.findGroupOf(it) } ?: GroupLookup.NoMembership }
            .getOrElse { return Result.Failure(it) }

        return when (lookup) {
            GroupLookup.NoMembership -> Result.Success(value = null)
            is GroupLookup.Found -> Result.Success(lookup.group.toDomain(GroupId(lookup.groupId)))
            // TODO(#38): log it, the store holds a membership no client can write under the rules.
            is GroupLookup.NotFound -> Result.Failure(GroupNotFound)
        }
    }

    override fun observeMembers(groupId: GroupId): Flow<List<Member>> =
        store.observeMembers(groupId.value).map { documents -> documents.mapNotNull(MemberDocument::toDomain) }

    override suspend fun create(group: Group, founder: UserId): Result<Unit, CreateGroupWriteError> = try {
        write(group, founder)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (e: Exception) {
        Result.Failure(CreateGroupWriteError.Unknown)
    }

    override suspend fun leaveMissingGroup(): Result<Unit, StoreUnavailable> = reachStore(::leave)

    private suspend fun write(group: Group, founder: UserId): Result<Unit, CreateGroupWriteError> {
        val credential = checkNotNull(users.credential()) { "A membership is written under a credential" }

        val lookup = reachStore { store.findGroupOf(credential) }.getOrElse { return Result.Failure(it) }

        when (lookup) {
            GroupLookup.NoMembership -> store.create(group.toDocument(), founder.toMembership(group.id, credential))
            is GroupLookup.Found -> return Result.Failure(CreateGroupWriteError.AlreadyInAGroup)
            is GroupLookup.NotFound -> return Result.Failure(GroupNotFound)
        }

        return Result.Success(Unit)
    }

    private suspend fun leave() {
        val credential = users.credential() ?: return

        val lookup = store.findGroupOf(credential)
        // TODO(#91): a group that exists is reported as left.
        if (lookup is GroupLookup.NotFound) store.leave(lookup.groupId, credential)
    }
}

private fun GroupDocument.toDomain(id: GroupId) = Group(
    id = id,
    name = name ?: GroupFactory.DEFAULT_NAME,
    createdAt = Instant.fromEpochMilliseconds(epochMilliseconds = createdAt ?: 0),
)

private fun MemberDocument.toDomain(): Member? {
    val userId = userId ?: return null

    val groupId = groupId ?: return null

    val joinedAt = joinedAt ?: return null

    return Member(
        userId = UserId(userId),
        groupId = GroupId(groupId),
        joinedAt = Instant.fromEpochMilliseconds(joinedAt),
    )
}

private fun Group.toDocument() = GroupDocument(id = id.value, name = name)

private fun UserId.toMembership(groupId: GroupId, credential: String) =
    MemberDocument(userId = value, groupId = groupId.value, credential = credential)
