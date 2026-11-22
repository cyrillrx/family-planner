package com.cyrillrx.family.app

import com.cyrillrx.logger.CompositeLogger
import com.cyrillrx.logger.ConsoleChild
import com.cyrillrx.logger.Log
import com.cyrillrx.logger.Severity

fun installConsoleLog() {
    Log.install(CompositeLogger(listOf(ConsoleChild(Severity.DEBUG))))
}
