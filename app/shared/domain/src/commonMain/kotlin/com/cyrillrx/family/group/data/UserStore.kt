package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.document.UserDocument

/**
 * The users this device can reach, each addressed by the credential that authenticates it.
 *
 * Every call may throw when the store refuses or cannot be reached. A read answers from the local
 * cache when the server is out of reach; a write returns once the server acknowledged it.
 */
interface UserStore {

    /** The credential this device holds, or null if it never signed in. Never signs in. */
    suspend fun credential(): String?

    /** A new anonymous credential, replacing whichever one this device held. */
    suspend fun signInAnonymously(): String

    suspend fun find(credential: String): UserDocument?

    /** Replaces whatever the credential addressed: a credential addresses one user at most. */
    suspend fun save(credential: String, document: UserDocument)
}
