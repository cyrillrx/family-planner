package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error

sealed interface GroupError : Error {
    sealed interface Read : GroupError
    sealed interface Write : GroupError
}

/** This device's membership names a group that does not exist: the member can only leave it. */
data object GroupNotFound : GroupError.Read, GroupError.Write

data object AlreadyInAGroup : GroupError.Write

/** The call ended without an answer, so it tells nothing about what the store holds. */
sealed interface NoAnswer : GroupError.Read, GroupError.Write, RegisterUserError {
    val cause: Throwable
}

/** The store could not be reached, or refused the call. */
data class StoreUnavailable(override val cause: Throwable) : NoAnswer

/** A defect of this client rather than a failure of the store. */
data class Unexpected(override val cause: Throwable) : NoAnswer
