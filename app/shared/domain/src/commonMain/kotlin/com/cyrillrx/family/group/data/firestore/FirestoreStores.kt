package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.data.UserStore

/** Null where Firebase is not initialized. */
expect fun firestoreUserStore(): UserStore?
