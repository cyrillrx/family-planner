package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.logger.Logger
import com.cyrillrx.logger.error
import com.cyrillrx.logger.warn

internal fun Logger.recordStoreFailure(failure: Error) {
    when (failure) {
        is ClientDefect -> error(LogTag.STORE, failure.cause) { "A defect of the client interrupted a store call" }
        is StoreUnavailable -> warn(LogTag.STORE, failure.cause) { "The store gave no answer" }
        GroupNotFound -> warn(LogTag.STORE) { "The membership points to a group the store does not hold" }
        else -> Unit
    }
}
