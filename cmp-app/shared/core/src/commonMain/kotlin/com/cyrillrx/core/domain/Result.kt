package com.cyrillrx.core.domain

sealed interface Result<out Data, out E : Error> {
    data class Success<out Data>(val value: Data) : Result<Data, Nothing>
    data class Failure<out E : Error>(val error: E) : Result<Nothing, E>
}
