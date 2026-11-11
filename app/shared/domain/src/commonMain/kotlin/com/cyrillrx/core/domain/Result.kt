package com.cyrillrx.core.domain

sealed interface Result<out Data, out E : Error> {
    data class Success<out Data>(val value: Data) : Result<Data, Nothing>
    data class Failure<out E : Error>(val error: E) : Result<Nothing, E>
}

inline fun <Data, E : Error, R> Result<Data, E>.map(transform: (Data) -> R): Result<R, E> = when (this) {
    is Result.Success -> Result.Success(transform(value))
    is Result.Failure -> this
}

inline fun <Data, E : Error> Result<Data, E>.getOrElse(onFailure: (E) -> Data): Data = when (this) {
    is Result.Success -> value
    is Result.Failure -> onFailure(error)
}

/** `null` on a failure, as on a success holding `null`. */
fun <Data> Result<Data, *>.getOrNull(): Data? = (this as? Result.Success)?.value
