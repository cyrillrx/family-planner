package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.data.UserStore
import com.cyrillrx.family.group.document.UserDocument
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore

internal class FirestoreUserStore(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : UserStore {

    override suspend fun credential(): String? = auth.currentUser?.uid

    override suspend fun signIn(): String = credential()
        ?: checkNotNull(auth.signInAnonymously().user) { "Anonymous sign-in answered without a user" }.uid

    override suspend fun find(credential: String): UserDocument? {
        val snapshot = users().document(credential).get()

        return if (snapshot.exists) snapshot.data<UserDocument>() else null
    }

    override suspend fun save(credential: String, document: UserDocument) {
        users().document(credential).set(document)
    }

    private fun users() = firestore.collection("users")
}
