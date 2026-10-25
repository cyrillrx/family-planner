package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.data.GroupStore
import com.cyrillrx.family.group.data.UserStore
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

actual fun firestoreUserStore(): UserStore? {
    val app = runCatching { Firebase.app }.getOrNull() ?: return null

    return FirestoreUserStore(Firebase.auth(app), Firebase.firestore(app))
}

actual fun firestoreGroupStore(): GroupStore? {
    val app = runCatching { Firebase.app }.getOrNull() ?: return null

    return FirestoreGroupStore(Firebase.firestore(app))
}
