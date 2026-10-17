package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.data.UserStore

/** Null where Firebase is not initialized, which leaves the application on its in-memory repositories. */
expect fun firestoreUserStore(): UserStore?
