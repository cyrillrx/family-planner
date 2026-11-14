package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.family.group.domain.GroupStore
import com.cyrillrx.family.group.domain.UserStore

expect fun firestoreUserStore(): UserStore?

expect fun firestoreGroupStore(): GroupStore?
