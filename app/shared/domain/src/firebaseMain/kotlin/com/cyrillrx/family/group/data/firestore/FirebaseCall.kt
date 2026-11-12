package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.StoreUnavailable
import dev.gitlive.firebase.FirebaseException

/** Reports a failure of Firebase as [StoreUnavailable], and lets any other exception through as a defect. */
internal inline fun <T> firebaseCall(call: () -> Result<T, StoreUnavailable>): Result<T, StoreUnavailable> = try {
    call()
} catch (e: FirebaseException) {
    Result.Failure(StoreUnavailable(e))
}

/** An absence only the local cache reports: the server may hold the document. */
internal fun unconfirmedAbsence(message: String) = Result.Failure(StoreUnavailable(UnconfirmedAbsence(message)))

private class UnconfirmedAbsence(message: String) : Exception(message)
