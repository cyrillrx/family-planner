package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.UserStore
import com.cyrillrx.family.group.document.UserDocument
import com.cyrillrx.family.group.domain.StoreUnavailable
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore

internal class FirestoreUserStore(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : UserStore {

    override suspend fun credential(): String? = auth.currentUser?.uid

    override suspend fun signInAnonymously(): Result<String, StoreUnavailable> = firebaseCall {
        val user = checkNotNull(auth.signInAnonymously().user) { "Anonymous sign-in answered without a user" }

        Result.Success(user.uid)
    }

    override suspend fun find(credential: String): Result<UserDocument?, StoreUnavailable> = firebaseCall {
        val snapshot = users().document(credential).get()

        if (snapshot.exists) return@firebaseCall Result.Success(snapshot.data<UserDocument>())

        if (snapshot.metadata.isFromCache) {
            return@firebaseCall unconfirmedAbsence("No user in the cache tells nothing of the server")
        }

        Result.Success(value = null)
    }

    override suspend fun save(credential: String, document: UserDocument): Result<Unit, StoreUnavailable> =
        firebaseCall {
            // TODO(#67): waiting for the server's acknowledgement blocks every caller while offline.
            users().document(credential).set(document)

            Result.Success(Unit)
        }

    private fun users() = firestore.collection("users")
}
