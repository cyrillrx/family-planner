package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
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

/** The group of the credential this device signed in with, which [UserStore] holds. */
class StoreGroupRepository(
    private val store: GroupStore,
    private val users: UserStore,
) : GroupRepository {

    /** Null when the store cannot tell, like [StoreUserRepository.registeredUserId]. */
    override suspend fun group(): Group? = try {
        users.credential()?.let { store.findGroupOf(it) }?.toDomain()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (e: Exception) {
        // TODO(#69): callers read this null as "not in a group".
        null
    }

    override fun observeMembers(groupId: GroupId): Flow<List<Member>> =
        store.observeMembers(groupId.value).map { documents -> documents.mapNotNull { it.toDomain() } }

    override suspend fun create(group: Group, founder: UserId): Result<Unit, CreateGroupWriteError> = try {
        val credential = users.credential() ?: return Result.Failure(CreateGroupWriteError.Unknown)

        store.create(group.toDocument(), founder.toMembership(group.id, credential))

        Result.Success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (e: Exception) {
        Result.Failure(CreateGroupWriteError.Unknown)
    }
}

private fun GroupDocument.toDomain(): Group? {
    val id = id ?: return null
    val name = name ?: return null
    val createdAt = createdAt ?: return null

    return Group(id = GroupId(id), name = name, createdAt = Instant.fromEpochMilliseconds(createdAt))
}

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
