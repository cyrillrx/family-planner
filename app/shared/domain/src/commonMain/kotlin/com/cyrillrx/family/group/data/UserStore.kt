package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.document.UserDocument
import com.cyrillrx.family.group.domain.StoreUnavailable

/**
 * The users this device can reach, each addressed by the credential that authenticates it.
 *
 * A read answers from the local cache when the server is out of reach, and fails when the cache
 * does not hold the document; a write returns once the server acknowledged it.
 */
interface UserStore {

    /** The credential this device holds, or null if it never signed in. Never signs in. */
    suspend fun credential(): String?

    /** A new anonymous credential, replacing whichever one this device held. */
    suspend fun signInAnonymously(): Result<String, StoreUnavailable>

    suspend fun find(credential: String): Result<UserDocument?, StoreUnavailable>

    /** Replaces whatever the credential addressed: a credential addresses one user at most. */
    suspend fun save(credential: String, document: UserDocument): Result<Unit, StoreUnavailable>
}
