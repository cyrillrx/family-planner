package com.cyrillrx.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ResultTest {

    @Test
    fun `maps the value of a success`() {
        assertEquals(Result.Success(value = 2), Result.Success(value = 1).map { it + 1 })
    }

    @Test
    fun `keeps the error of a failure through a map`() {
        val failure: Result<Int, Refusal> = Result.Failure(Refusal)

        assertEquals(Result.Failure(Refusal), failure.map { it + 1 })
    }

    @Test
    fun `gives the value of a success rather than the fallback`() {
        assertEquals(1, Result.Success(value = 1).getOrElse { 2 })
    }

    @Test
    fun `gives the fallback for a failure`() {
        val failure: Result<Int, Refusal> = Result.Failure(Refusal)

        assertEquals(2, failure.getOrElse { 2 })
    }

    @Test
    fun `unwraps the success held by a success`() {
        val nested: Result<Result<Int, Refusal>, Refusal> = Result.Success(Result.Success(1))

        assertEquals(Result.Success(1), nested.flatten())
    }

    @Test
    fun `unwraps the failure held by a success`() {
        val nested: Result<Result<Int, Refusal>, Refusal> = Result.Success(Result.Failure(Refusal))

        assertEquals(Result.Failure(Refusal), nested.flatten())
    }

    @Test
    fun `keeps the error of a failure through a flatten`() {
        val failure: Result<Result<Int, Refusal>, Refusal> = Result.Failure(Refusal)

        assertEquals(Result.Failure(Refusal), failure.flatten())
    }

    private data object Refusal : Error
}
