package com.cyrillrx.family.firebase

/**
 * A no-op where Firebase is not wired yet, or where `local.properties` carries no options — the
 * repository is public and holds none. The application then stays on its in-memory repositories
 * rather than failing to start.
 */
expect fun initializeFirebase(context: Any? = null)
