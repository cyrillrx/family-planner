package com.cyrillrx.family.group.domain

import com.cyrillrx.logger.CompositeLogger
import com.cyrillrx.logger.Logger
import com.cyrillrx.logger.RamLogChild
import com.cyrillrx.logger.Severity

internal class RecordingLogger(private val child: RamLogChild = RamLogChild()) : Logger by CompositeLogger(
    listOf(child),
) {
    fun recorded(): List<Recorded> = child.entries().map { Recorded(it.severity, it.tag, it.throwable) }
}

internal data class Recorded(val severity: Severity, val tag: String, val throwable: Throwable? = null)
