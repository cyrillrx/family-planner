package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.domain.GroupStore
import com.cyrillrx.family.group.domain.UserStore

// TODO(#47): the Firebase SDK is not linked on iOS yet.
actual fun firestoreUserStore(): UserStore? = null

// TODO(#47): the Firebase SDK is not linked on iOS yet.
actual fun firestoreGroupStore(): GroupStore? = null
