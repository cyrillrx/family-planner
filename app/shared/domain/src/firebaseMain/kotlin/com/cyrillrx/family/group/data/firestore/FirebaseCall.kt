package com.cyrillrx.family.group.data.firestore

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.StoreUnavailable
import dev.gitlive.firebase.FirebaseException

internal inline fun <T> callFirebase(call: () -> Result<T, StoreUnavailable>): Result<T, StoreUnavailable> = try {
    call()
} catch (failure: FirebaseException) {
    // TODO(#99): tell a refused call apart from an unreachable store.
    Result.Failure(StoreUnavailable(failure))
}

internal fun failWithUnconfirmedAbsence(message: String) = Result.Failure(StoreUnavailable(UnconfirmedAbsence(message)))

private class UnconfirmedAbsence(message: String) : Exception(message)
