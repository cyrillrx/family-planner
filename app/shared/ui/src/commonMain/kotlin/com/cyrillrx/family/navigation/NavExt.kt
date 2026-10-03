package com.cyrillrx.family.navigation

import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/** No-op on the root entry: `NavDisplay` requires a back stack that is never empty. */
fun NavBackStack<NavKey>.navigateUp() {
    if (size > 1) removeLastOrNull()
}

/** One mutation: `NavDisplay` requires a back stack that is never empty. */
fun NavBackStack<NavKey>.replaceAllWith(key: NavKey) {
    Snapshot.withMutableSnapshot {
        clear()
        add(key)
    }
}
