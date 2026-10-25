package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.data.GroupStore
import com.cyrillrx.family.group.data.UserStore

/** Null where Firebase is not initialized. */
expect fun firestoreUserStore(): UserStore?

/** Null where Firebase is not initialized. */
expect fun firestoreGroupStore(): GroupStore?
