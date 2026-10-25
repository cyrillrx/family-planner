package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.GroupStore
import com.cyrillrx.family.group.data.UserStore
import com.cyrillrx.family.group.document.GroupDocument
import com.cyrillrx.family.group.document.MemberDocument
import com.cyrillrx.family.group.document.UserDocument
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.Member
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Instant

class StoreGroupRepositoryTest {

    @Test
    fun `has no group on a device that never signed in`() = runTest {
        val store = FakeGroupStore().apply { groupsByCredential["uid-1"] = storedGroup() }

        assertNull(StoreGroupRepository(store, CredentialOnly(null)).group())
    }

    @Test
    fun `has no group when the credential is a member of none`() = runTest {
        assertNull(StoreGroupRepository(FakeGroupStore(), CredentialOnly("uid-1")).group())
    }

    @Test
    fun `finds the group the credential is a member of`() = runTest {
        val store = FakeGroupStore().apply { groupsByCredential["uid-1"] = storedGroup() }

        assertEquals(group(), StoreGroupRepository(store, CredentialOnly("uid-1")).group())
    }

    @Test
    fun `reads a stored group missing a field as no group`() = runTest {
        val store = FakeGroupStore().apply { groupsByCredential["uid-1"] = storedGroup().copy(createdAt = null) }

        assertNull(StoreGroupRepository(store, CredentialOnly("uid-1")).group())
    }

    @Test
    fun `has no group when the store cannot be read`() = runTest {
        val store = FakeGroupStore(failure = IllegalStateException("unreachable"))

        assertNull(StoreGroupRepository(store, CredentialOnly("uid-1")).group())
    }

    @Test
    fun `writes the group and its founder under the credential of this device`() = runTest {
        val store = FakeGroupStore()

        StoreGroupRepository(store, CredentialOnly("uid-1")).create(group(), founder = UserId("user-1"))

        assertEquals(
            GroupDocument(id = "group-1", name = "Family") to
                MemberDocument(userId = "user-1", groupId = "group-1", credential = "uid-1"),
            store.creations.single(),
        )
    }

    @Test
    fun `answers a successful creation`() = runTest {
        assertEquals(
            Result.Success(Unit),
            StoreGroupRepository(FakeGroupStore(), CredentialOnly("uid-1")).create(group(), UserId("user-1")),
        )
    }

    @Test
    fun `refuses to create a group without a credential to write it under`() = runTest {
        val store = FakeGroupStore()

        val result = StoreGroupRepository(store, CredentialOnly(null)).create(group(), UserId("user-1"))

        assertEquals(Result.Failure(CreateGroupWriteError.Unknown), result)
        assertEquals(emptyList(), store.creations)
    }

    @Test
    fun `reports a store that refuses the creation`() = runTest {
        val store = FakeGroupStore(failure = IllegalStateException("permission denied"))

        assertEquals(
            Result.Failure(CreateGroupWriteError.Unknown),
            StoreGroupRepository(store, CredentialOnly("uid-1")).create(group(), UserId("user-1")),
        )
    }

    @Test
    fun `lets a cancellation through rather than reporting it`() = runTest {
        val store = FakeGroupStore(failure = CancellationException("left the screen"))

        assertFailsWith<CancellationException> {
            StoreGroupRepository(store, CredentialOnly("uid-1")).create(group(), UserId("user-1"))
        }
    }

    @Test
    fun `observes the members of the group`() = runTest {
        val store = FakeGroupStore()
        store.members.value = listOf(
            MemberDocument(userId = "user-1", groupId = "group-1", credential = "uid-1", joinedAt = MILLIS),
        )

        assertEquals(
            listOf(Member(userId = UserId("user-1"), groupId = GroupId("group-1"), joinedAt = CREATED_AT)),
            StoreGroupRepository(store, CredentialOnly("uid-1")).observeMembers(GroupId("group-1")).first(),
        )
    }

    @Test
    fun `leaves out a membership missing a field`() = runTest {
        val store = FakeGroupStore()
        store.members.value = listOf(MemberDocument(userId = "user-1", groupId = "group-1", credential = "uid-1"))

        assertEquals(
            emptyList(),
            StoreGroupRepository(store, CredentialOnly("uid-1")).observeMembers(GroupId("group-1")).first(),
        )
    }

    private fun group() = Group(id = GroupId("group-1"), name = "Family", createdAt = CREATED_AT)

    private fun storedGroup() = GroupDocument(id = "group-1", name = "Family", createdAt = MILLIS)

    private companion object {
        const val MILLIS = 1_760_000_000_000
        val CREATED_AT = Instant.fromEpochMilliseconds(MILLIS)
    }
}

private class FakeGroupStore(private val failure: Exception? = null) : GroupStore {

    val groupsByCredential = mutableMapOf<String, GroupDocument>()
    val creations = mutableListOf<Pair<GroupDocument, MemberDocument>>()
    val members = MutableStateFlow<List<MemberDocument>>(emptyList())

    override suspend fun findGroupOf(credential: String): GroupDocument? {
        failure?.let { throw it }

        return groupsByCredential[credential]
    }

    override suspend fun create(group: GroupDocument, founder: MemberDocument) {
        failure?.let { throw it }

        creations += group to founder
    }

    override fun observeMembers(groupId: String): Flow<List<MemberDocument>> = members
}

/** Holds a credential and nothing else: the group repository reads no user. */
private class CredentialOnly(private val credential: String?) : UserStore {

    override suspend fun credential(): String? = credential

    override suspend fun signInAnonymously(): String = error("The group repository never signs in")

    override suspend fun find(credential: String): UserDocument? = error("The group repository reads no user")

    override suspend fun save(credential: String, document: UserDocument) = error("The group repository writes no user")
}
