package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error

sealed interface GroupError : Error {
    sealed interface Read : GroupError
    sealed interface Write : GroupError
}

data object GroupNotFound : GroupError.Read, GroupError.Write

data object AlreadyInAGroup : GroupError.Write

sealed interface NoAnswer : GroupError.Read, GroupError.Write, RegisterUserError {
    val cause: Throwable
}

data class StoreUnavailable(override val cause: Throwable) : NoAnswer

data class ClientDefect(override val cause: Throwable) : NoAnswer
