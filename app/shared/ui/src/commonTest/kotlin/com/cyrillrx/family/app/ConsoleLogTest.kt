package com.cyrillrx.family.app

import com.cyrillrx.logger.Log
import com.cyrillrx.logger.Severity
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsoleLogTest {

    @Test
    fun `writes from the debug severity up once installed`() {
        installConsoleLog()

        assertEquals(
            listOf(false, true),
            listOf(Severity.VERBOSE, Severity.DEBUG).map { Log.isLoggable(it, "Store") },
        )
    }
}
