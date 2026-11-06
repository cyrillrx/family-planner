package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.data.GroupStore
import com.cyrillrx.family.group.data.UserStore

// TODO(#47): the Firebase SDK is not linked on iOS yet.
actual fun firestoreUserStore(): UserStore? = null

// TODO(#47): the Firebase SDK is not linked on iOS yet.
actual fun firestoreGroupStore(): GroupStore? = null
