package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.data.GroupStore
import com.cyrillrx.family.group.data.UserStore

expect fun firestoreUserStore(): UserStore?

expect fun firestoreGroupStore(): GroupStore?
