package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.data.GroupLookup
import com.cyrillrx.family.group.data.GroupStore
import com.cyrillrx.family.group.document.GroupDocument
import com.cyrillrx.family.group.document.MemberDocument
import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.toDuration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

internal class FirestoreGroupStore(private val firestore: FirebaseFirestore) : GroupStore {

    override suspend fun findGroupOf(credential: String): GroupLookup {
        val memberships = firestore.collectionGroup(MEMBERS)
            .where { "credential" equalTo credential }
            .limit(1)
            .get()
        val membership = memberships.documents.firstOrNull()
        if (membership == null) {
            check(!memberships.metadata.isFromCache) { "No membership in the cache tells nothing of the server" }
            return GroupLookup.NoMembership
        }

        // From the path rather than the field: the path is always there.
        val groupId = checkNotNull(membership.reference.parent.parent) { "A membership sits under a group" }.id
        val group = groups().document(groupId).get()
        if (group.exists) return GroupLookup.Found(groupId, group.toGroup())

        check(!group.metadata.isFromCache) { "No group in the cache tells nothing of the server" }
        return GroupLookup.NotFound(groupId)
    }

    override suspend fun create(group: GroupDocument, founder: MemberDocument) {
        val groupReference = groups().document(checkNotNull(group.id) { "A group is addressed by its id" })
        val founderReference = groupReference.collection(MEMBERS)
            .document(checkNotNull(founder.credential) { "A membership is addressed by its credential" })

        // TODO(#67): waiting for the server's acknowledgement blocks every caller while offline.
        firestore.batch()
            .set(groupReference, StoredGroup(id = group.id, name = group.name, createdAt = Timestamp.ServerTimestamp))
            .set(
                founderReference,
                StoredMember(
                    userId = founder.userId,
                    groupId = founder.groupId,
                    credential = founder.credential,
                    joinedAt = Timestamp.ServerTimestamp,
                ),
            )
            .commit()
    }

    override suspend fun leave(groupId: String, credential: String) {
        // TODO(#67): waiting for the server's acknowledgement blocks every caller while offline.
        groups().document(groupId).collection(MEMBERS).document(credential).delete()
    }

    override fun observeMembers(groupId: String): Flow<List<MemberDocument>> =
        groups().document(groupId).collection(MEMBERS).snapshots.map { snapshot ->
            snapshot.documents.map(DocumentSnapshot::toMember)
        }

    private fun groups() = firestore.collection("groups")

    private companion object {
        const val MEMBERS = "members"
    }
}

@Serializable
private class StoredGroup(
    val id: String? = null,
    val name: String? = null,
    val createdAt: BaseTimestamp? = null,
)

@Serializable
private class StoredMember(
    val userId: String? = null,
    val groupId: String? = null,
    val credential: String? = null,
    val joinedAt: BaseTimestamp? = null,
)

private fun DocumentSnapshot.toGroup(): GroupDocument {
    val stored = data<StoredGroup>(ServerTimestampBehavior.ESTIMATE)

    return GroupDocument(id = stored.id, name = stored.name, createdAt = stored.createdAt.toEpochMilliseconds())
}

private fun DocumentSnapshot.toMember(): MemberDocument {
    val stored = data<StoredMember>(ServerTimestampBehavior.ESTIMATE)

    return MemberDocument(
        userId = stored.userId,
        groupId = stored.groupId,
        credential = stored.credential,
        joinedAt = stored.joinedAt.toEpochMilliseconds(),
    )
}

private fun BaseTimestamp?.toEpochMilliseconds(): Long? =
    (this as? Timestamp)?.toDuration()?.inWholeMilliseconds
